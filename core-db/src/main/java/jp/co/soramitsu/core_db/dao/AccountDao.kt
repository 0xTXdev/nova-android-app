package jp.co.soramitsu.core_db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import jp.co.soramitsu.core_db.model.AccountLocal

@Dao
abstract class AccountDao {

    @Query("select * from users order by networkType, position")
    abstract fun observeAccounts(): Observable<List<AccountLocal>>

    @Query("select * from users where address = :address")
    abstract fun getAccount(address: String): Single<AccountLocal>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract fun insert(account: AccountLocal): Long

    @Query("DELETE FROM users where address = :address")
    abstract fun remove(address: String) : Completable

    @Update
    abstract fun updateAccount(account: AccountLocal): Completable

    @Update
    abstract fun updateAccounts(accounts: List<AccountLocal>) : Completable

    @Query("SELECT COALESCE(MAX(position), 0)  + 1 from users")
    abstract fun getNextPosition() : Int
}
package jp.co.soramitsu.core_db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import io.reactivex.Observable
import jp.co.soramitsu.core_db.model.TransactionLocal

@Dao
abstract class TransactionDao {

    @Query("SELECT * FROM transactions WHERE accountAddress = :accountAddress")
    abstract fun observeTransactions(accountAddress: String): Observable<List<TransactionLocal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract fun insert(transactions: List<TransactionLocal>)

    @Query("DELETE FROM transactions WHERE accountAddress = :accountAddress")
    protected abstract fun clear(accountAddress: String)

    @Transaction
    open fun clearAndInsert(accountAddress: String, transactions: List<TransactionLocal>) {
        clear(accountAddress)
        insert(transactions)
    }
}
package jp.co.soramitsu.core_db.di

import jp.co.soramitsu.core_db.AppDatabase
import jp.co.soramitsu.core_db.dao.AccountDao
import jp.co.soramitsu.core_db.dao.AssetDao
import jp.co.soramitsu.core_db.dao.NodeDao

interface DbApi {

    fun provideDatabase(): AppDatabase

    fun provideAccountDao(): AccountDao

    fun provideNodeDao(): NodeDao

    fun provideAssetDao(): AssetDao
}
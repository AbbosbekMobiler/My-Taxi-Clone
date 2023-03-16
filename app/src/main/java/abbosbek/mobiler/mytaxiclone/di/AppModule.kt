package abbosbek.mobiler.mytaxiclone.di

import abbosbek.mobiler.mytaxiclone.db.UserDao
import abbosbek.mobiler.mytaxiclone.db.UserDatabase
import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideUserDatabase(@ApplicationContext context: Context) =
        Room.databaseBuilder(
            context,
            UserDatabase::class.java,
            "user_address.db"
        ).build()

    @Provides
    @Singleton
    fun provideUserDao(userDatabase: UserDatabase) : UserDao{
        return userDatabase.getUserDao()
    }

}
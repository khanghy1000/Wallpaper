package com.example.wallpaper.di;

import com.example.wallpaper.Constants;
import com.example.wallpaper.data.network.NetworkWallhavenService;
import com.example.wallpaper.data.network.model.serializer.InstantJsonAdapter;
import com.example.wallpaper.data.network.model.serializer.NetworkWallhavenMetaQuerySerializer;
import com.squareup.moshi.Moshi;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;

@Module
@InstallIn(SingletonComponent.class)
public class NetworkModule {
    @Provides
    @Singleton
    public Moshi provideMoshi() {
        return new Moshi.Builder()
                .add(new InstantJsonAdapter())
                .add(new NetworkWallhavenMetaQuerySerializer())
                .build();
    }

    @Provides
    @Singleton
    public Retrofit provideRetrofit(Moshi moshi) {
        return new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build();
    }

    @Provides
    @Singleton
    public NetworkWallhavenService provideNetworkWallhavenService(Retrofit retrofit) {
        return retrofit.create(NetworkWallhavenService.class);
    }
}

package com.example.wallpaper.di;

import com.example.wallpaper.Constants;
import com.example.wallpaper.data.network.DocumentConverterFactory;
import com.example.wallpaper.data.network.NetworkWallhavenApi;
import com.example.wallpaper.data.network.NetworkWallhavenDataSource;
import com.example.wallpaper.data.network.model.serializer.InstantJsonAdapter;
import com.example.wallpaper.data.network.model.serializer.NetworkWallhavenMetaQuerySerializer;
import com.squareup.moshi.Moshi;

import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
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
    public OkHttpClient provideOkHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .connectTimeout(30, TimeUnit.SECONDS);

            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(loggingInterceptor);

        return builder.build();
    }

    @Provides
    @Singleton
    public Retrofit provideRetrofit(Moshi moshi, OkHttpClient okHttpClient) {
        return new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(new DocumentConverterFactory())
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build();
    }

    @Provides
    @Singleton
    public NetworkWallhavenApi provideNetworkWallhavenApi(Retrofit retrofit) {
        return retrofit.create(NetworkWallhavenApi.class);
    }

    @Provides
    @Singleton
    public NetworkWallhavenDataSource provideNetworkWallhavenDataSource(NetworkWallhavenApi networkWallhavenApi) {
        return new NetworkWallhavenDataSource(networkWallhavenApi);
    }
}

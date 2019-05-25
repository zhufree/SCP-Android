/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package info.free.scp.util

import android.content.Context
import info.free.scp.view.download.DownloadListViewModelFactory
import info.free.scp.view.download.DownloadRepository
import info.free.scp.view.feed.FeedListViewModelFactory
import info.free.scp.view.feed.FeedRepository

/**
 * Static methods used to inject classes needed for various Activities and Fragments.
 */
object InjectorUtils {

    private fun getFeedRepository(context: Context): FeedRepository {
        return FeedRepository()
    }
    private fun getDownloadRepository(): DownloadRepository {
        return DownloadRepository()
    }
//    private fun getPictureRepository(context: Context): PictureRepository {
//        return PictureRepository()
//    }


    fun provideFeedListViewModelFactory(context: Context): FeedListViewModelFactory {
        val repository = getFeedRepository(context)
        return FeedListViewModelFactory(repository)
    }
    fun provideDownloadListViewModelFactory(): DownloadListViewModelFactory {
        val repository = getDownloadRepository()
        return DownloadListViewModelFactory(repository)
    }

//    fun providePlantDetailViewModelFactory(
//        context: Context,
//        plantId: String
//    ): PlantDetailViewModelFactory {
//        return PlantDetailViewModelFactory(getArticleRepository(context),
//                getGardenPlantingRepository(context), plantId)
//    }
}

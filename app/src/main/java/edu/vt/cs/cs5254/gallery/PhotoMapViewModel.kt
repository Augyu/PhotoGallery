package edu.vt.cs.cs5254.gallery

import androidx.lifecycle.ViewModel

class PhotoMapViewModel :ViewModel(){
    val galleryItemLiveData = FlickrFetchr.fetchPhotos()
}
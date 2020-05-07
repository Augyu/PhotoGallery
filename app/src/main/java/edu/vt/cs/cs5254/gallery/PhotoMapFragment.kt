package edu.vt.cs.cs5254.gallery

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import edu.vt.cs.cs5254.gallery.api.GalleryItem

class PhotoMapFragment : MapViewFragment(), GoogleMap.OnMarkerClickListener {

    private lateinit var photoMapViewModel: PhotoMapViewModel
    private lateinit var thumbnailDownloader: ThumbnailDownloader<Marker>
    var geoGalleryItemMap = emptyMap<String, GalleryItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        photoMapViewModel = ViewModelProvider(this).get(PhotoMapViewModel::class.java)
        val responseHandler = Handler()
        thumbnailDownloader = ThumbnailDownloader(responseHandler) { marker, bitmap ->
            setMarkerIcon(marker, bitmap)
        }
        lifecycle.addObserver(thumbnailDownloader.fragmentLifecycleObserver)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateMapView(
            inflater,
            container,
            savedInstanceState,
            R.layout.fragment_photo_map,
            R.id.map_view
        )
        lifecycle.addObserver(thumbnailDownloader.viewLifecycleObserver)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onMapViewCreated(view, savedInstanceState) { googleMap ->
            googleMap.setOnMarkerClickListener(this@PhotoMapFragment)
        }
        photoMapViewModel.galleryItemLiveData.observe(
            viewLifecycleOwner,
            Observer { galleryItems ->
                updateUI(galleryItems)
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewLifecycleOwner.lifecycle.removeObserver(
            thumbnailDownloader.viewLifecycleObserver
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycle.removeObserver(
            thumbnailDownloader.fragmentLifecycleObserver
        )
    }

    private fun updateUI(galleryItems: List<GalleryItem>) {
        Log.d("test", "inUpdateUI")
        if (!isAdded || galleryItems.isEmpty()) {
            return
        }
        googleMap.clear()
        val bounds = LatLngBounds.Builder()
        geoGalleryItemMap = galleryItems.filterNot { it.latitude == "0" && it.longitude == "0" }.associateBy { it.id }

        for (item in geoGalleryItemMap.values) {
            val itemPoint = LatLng(item.latitude.toDouble(), item.longitude.toDouble())
            bounds.include(itemPoint)

            val itemMarker = MarkerOptions().position(itemPoint).title(item.title)
            val marker = googleMap.addMarker(itemMarker)
            marker.tag = item.id

            thumbnailDownloader.queueThumbnail(marker, item.url)
        }
    }

    override fun onMarkerClick(marker: Marker?): Boolean {
//        val galleryItemId = marker?.tag as String
//        val item = geoGalleryItemMap.get(galleryItemId)
//        val uri = item?.photoPageUri ?: return false
//        val intent = PhotoPageActivity.newIntent(requireContext(), uri)
//        startActivity(intent)
        return true
    }

}

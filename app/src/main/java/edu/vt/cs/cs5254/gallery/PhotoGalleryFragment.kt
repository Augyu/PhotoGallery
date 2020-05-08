package edu.vt.cs.cs5254.gallery

import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.vt.cs.cs5254.gallery.api.GalleryItem
import kotlinx.android.synthetic.main.fragment_photo_gallery.*


class PhotoGalleryFragment : Fragment() {
    private lateinit var photoGalleryViewModel: PhotoGalleryViewModel
    private lateinit var thumbnailDownloader: ThumbnailDownloader<PhotoHolder>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        photoGalleryViewModel = ViewModelProvider(this).get(PhotoGalleryViewModel::class.java)
        photoGalleryViewModel.loadPhotos()
        val responseHandler = Handler()
        thumbnailDownloader = ThumbnailDownloader(responseHandler) { photoHolder, bitmap ->
            val drawable = BitmapDrawable(resources, bitmap)
            photoHolder.bindDrawable(drawable)
        }
        lifecycle.addObserver(thumbnailDownloader.fragmentLifecycleObserver)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewLifecycleOwner.lifecycle.addObserver(
            thumbnailDownloader.viewLifecycleObserver
        )
        return inflater.inflate(R.layout.fragment_photo_gallery, container, false)
//        photoRecyclerView = view.findViewById(R.id.photo_recycler_view)
//        photoRecyclerView.layoutManager = GridLayoutManager(context, 3)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        photo_recycler_view.layoutManager = GridLayoutManager(context, 3)
        photoGalleryViewModel.galleryItemsLiveData.observe(
            viewLifecycleOwner,
            Observer { galleryItems ->
                photo_recycler_view.adapter = PhotoAdapter(galleryItems)
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

    private inner class PhotoHolder(
        itemImageView: ImageView,
        onHolderClickListener: OnHolderClickListener
    ) : RecyclerView.ViewHolder(itemImageView) {
        val bindDrawable: (Drawable) -> Unit = itemImageView::setImageDrawable

        init {
            itemImageView.setOnClickListener {
                onHolderClickListener.onClickListener(adapterPosition)
            }
        }
    }

    private inner class PhotoAdapter(private val galleryItems: List<GalleryItem>) :
        RecyclerView.Adapter<PhotoHolder>(), OnHolderClickListener {
        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): PhotoHolder {
            val view =
                layoutInflater.inflate(R.layout.list_item_gallery, parent, false) as ImageView
            return PhotoHolder(view, this)
        }

        override fun getItemCount(): Int = galleryItems.size

        override fun onBindViewHolder(holder: PhotoHolder, position: Int) {
            val galleryItem = galleryItems[position]
            val placeholder: Drawable = ContextCompat.getDrawable(
                requireContext(),
                R.drawable.placeholder
            ) ?: ColorDrawable()
            holder.bindDrawable(placeholder)
            thumbnailDownloader.queueThumbnail(holder, galleryItem.url)
        }

        override fun onClickListener(position: Int) {
            val intent =
                PhotoPageActivity.newIntent(requireContext(), galleryItems[position].photoPageUri)
            startActivity(intent)
        }
    }

    interface OnHolderClickListener {
        fun onClickListener(position: Int) {}
    }
}
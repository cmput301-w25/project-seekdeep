package com.example.project_seekdeep.Helpers;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

/**
 *  This class is in charge of uploading and downloading images and interacting with firebase Storage
 *  Most of this code is kinda copied from MoodProvider
 *  @author Nancy Lin
 */
public class ImageProvider {
    //Attributes:
    //note: the StorageReference instance will not be an attribute, but be passed to ImageProvider (so there's no accidental mix up of possible firebase storage instances)
    private static ImageProvider imageProvider;
    private final StorageReference imageStorageRef; //final means that imageStorageRef cannot be reassigned throughout ImageProvider's lifetime


    //Use constructor to initialize the attributes to firebase Storage:
    /**
     * This is a constructor for ImageProvider
     * @param storage   The FirebaseStorage to get images from
     */
    public ImageProvider(FirebaseStorage storage) {

        // Connect imageStorageRef to firebase storage
        imageStorageRef = storage.getReference().child("Images/");

    }

    /**
     * This checks if a ImageProvider already exists. If not, make a new instance of ImageProvider
     *
     * @param storage      FirebaseStorage instance
     * @return ImageProvider (either an existing instance or a new instance)
     */
    public static ImageProvider getInstance(FirebaseStorage storage){
        if (imageProvider == null)
            imageProvider = new ImageProvider(storage);
        return imageProvider;
    }


    /**
     * This uploads an image to the Firebase Storage
     *
     * @param selectedImage      The Uri of the image to be uploaded
     */
    public void uploadImageToFirebase(Uri selectedImage) {
        StorageReference selectedImageRef = imageStorageRef.child(selectedImage.getLastPathSegment());
        UploadTask uploadTask = selectedImageRef.putFile(selectedImage);

        // Register observers to listen for when the upload is done or if it fails
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                // ...
            }
        });
    }

    /**
     * Delete an image from firebase using the Uri / last path segment
     *
     * @param selectedImage The image Uri that corresponds to the image to be deleted
     */
    public void deleteImageFromFirebase(Uri selectedImage){
        StorageReference selectedImageRef = imageStorageRef.child(selectedImage.getLastPathSegment());
        selectedImageRef.delete();
    }


    /**
     * Get the StorageReference to the firebase Storage/Images folder
     *
     * @return the StorageReference to the firebase Storage/Images folder
     */
    public StorageReference getImagesStorageRef(){
        return imageStorageRef;
    }

    /**
     * Get the StorageReference to an image using the lastPathSeg of a Uri
     * @param lastPathSeg   String lastPathSeg is kinda the identifier for the storage reference
     * @return  The storage reference to the image you get using the lastPathSeg
     */
    public StorageReference getStorageRefFromLastPathSeg(String lastPathSeg){
        return imageStorageRef.child(lastPathSeg);
    }




}

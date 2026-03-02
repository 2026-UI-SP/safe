package safe

import grails.config.Config
import grails.core.support.GrailsConfigurationAware
import groovy.transform.CompileStatic

import java.time.Instant;
import org.springframework.web.multipart.MultipartFile

import grails.gorm.transactions.Transactional

// @Transactional
class PhotoUploadService implements GrailsConfigurationAware {

    PhotoService photoService

    String photoFolder

    @Override
    void setConfiguration(Config co) {
        photoFolder = co.getRequiredProperty('contentFolder')
    }

    def uploadFile(Photo photo) {
        //  println ""; println ""; 
        //  println "uploadFile called with photo: ${photo}"
        
        File folder = new File(photoFolder)
        if ( !folder.exists() ) {
            println "Creating folder: ${photoFolder}"
            folder.mkdirs()
        }

        // Write the file to the file system
        MultipartFile file = photo.photoFile
        long time = Instant.now().toEpochMilli();
        String extension = file.originalFilename.substring(file.originalFilename.lastIndexOf('.') + 1)
        String path = "${photoFolder}/${time}.${extension}"
        photo.photoFile.transferTo(new File(path))

        // Complete the Photo domain object
        photo.photoPath = path
        photo.photoContentType = file.contentType
        photo.createTime = time

        // Save the Photo domain object
        photo = photoService.save(photo)  

        if ( !photo || photo.hasErrors() ) {
            println "Error saving photo: ${photo?.errors}"
            File f = new File(path)
            f.delete()
            respond photo?.errors ?: [photo: photo], model: [photo: photo], view:'create'  // Just respond with the photo and its errors. The view can access the photo and its errors directly.
            return
        }
        photo
    }
}

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

    def uploadFile(PhotoFileCommand photoFileCmd) {
        // println ""; println ""; 
        // println "Uploading file: ${photoFileCmd.photoFile.originalFilename} to folder: ${photoFolder}"

        File folder = new File(photoFolder)
        if ( !folder.exists() ) {
            println "Creating folder: ${photoFolder}"
            folder.mkdirs()
        }

        
        MultipartFile file = photoFileCmd.photoFile
        // Path to write the file
        long time = Instant.now().toEpochMilli();
        String extension = file.originalFilename.substring(file.originalFilename.lastIndexOf('.')+1)
        String path = "${photoFolder}/${time}.${extension}"
        // Writes the file 
        photoFileCmd.photoFile.transferTo(new File(path))

        String photoPath = "${path}"
        Photo photo = photoService.save(photoPath, file.contentType, time)
        println "Photo saved: ${photo}"

        if ( !photo || photo.hasErrors() ) {
            println "Error saving photo: ${photo?.errors}"
            File f = new File(path)
            f.delete()
        }
        println "Returning photo: ${photo}"
        photo
    }
}

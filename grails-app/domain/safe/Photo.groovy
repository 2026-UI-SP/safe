package safe

import org.springframework.web.multipart.MultipartFile
class Photo {

    long createTime
    String photoPath
    String photoContentType
    MultipartFile photoFile

    static transients = ['photoFile']

    MultipartFile getPhotoFile() {
        return photoFile
    }

    void setPhotoFile(MultipartFile photoFile) {
        this.photoFile = photoFile
    }   

    static constraints = {
        createTime nullable: true
        photoPath nullable: true, blank: true
        photoContentType nullable: true, blank: true 
        photoFile  validator: { val, obj ->
            if ( val == null ) {
                return false
            }
            if ( val.empty ) {
                return false
            }

            ['jpeg', 'jpg'].any { extension -> 
                 val.originalFilename?.toLowerCase()?.endsWith(extension)
            }
        }
    }
}

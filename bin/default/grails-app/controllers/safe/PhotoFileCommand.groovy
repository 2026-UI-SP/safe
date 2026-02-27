package safe

import grails.validation.Validateable
import org.springframework.web.multipart.MultipartFile

class PhotoFileCommand implements Validateable {
    MultipartFile photoFile

    static constraints = {
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
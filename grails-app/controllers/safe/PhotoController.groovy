package safe

import grails.validation.ValidationException
import static org.springframework.http.HttpStatus.*
import org.springframework.validation.FieldError

import org.springframework.web.multipart.MultipartFile

class PhotoController {

    PhotoService photoService
    PhotoUploadService photoUploadService
    SafeService safeService

    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond photoService.list(params), model:[photoCount: photoService.count()]
    }

    def show(Long id) {
        respond photoService.get(id)
    }

    def photoImage(Long id) {
        Photo photo = photoService.get(id)
        if (!photo) {
            notFound()
            return
        }
        def file = new File(photo.photoPath)
        render file: file, contentType: photo.photoContentType
    }

    def create() {
        // println ""; println ""; println "create called with params: ${params}"
        respond new Photo(params)  
    }

    def save(Photo photo) {
        println ""; println ""; println "save called with photo: ${photo}"; println "save called with params: ${params}"
        if (photo == null) {
            notFound()
            return
        }
        // The Domain validation does not acturally throw a ValidationException, 
        // so we need to check for errors 
        if (photo.hasErrors()) {  
            respond photo.errors, model: [photo: photo], view:'create'  
            return
        }

        try { 
            // This can throw a ValidationException if the photo is not safe.
            safeService.moderatePhoto(photo)  
        } catch (ValidationException e) {
            respond photo, model: [photo: photo], view:'create'  
            return
        }

        // Save the file and enter the photo metadata in the DB.
        photo = photoUploadService.uploadFile(photo)

        if (photo == null || photo.hasErrors()) {
            respond photo?.errors ?: [photo: photo], model: [photo: photo], view:'create'  
            return
        }

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.created.message', args: [message(code: 'photo.label', default: 'Photo'), photo.id])
                redirect photo
            }
            '*' { respond photo, [status: CREATED] }
        }
    }

    def edit(Long id) {
        respond photoService.get(id)
    }

    def update(Photo photo) {
        if (photo == null) {
            notFound()
            return
        }

        try {
            photoService.save(photo)
        } catch (ValidationException e) {
            respond photo.errors, view:'edit'
            return
        }

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'photo.label', default: 'Photo'), photo.id])
                redirect photo
            }
            '*'{ respond photo, [status: OK] }
        }
    }

    def delete(Long id) {
        if (id == null) {
            notFound()
            return
        }

        photoService.delete(id)

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.deleted.message', args: [message(code: 'photo.label', default: 'Photo'), id])
                redirect action:"index", method:"GET"
            }
            '*'{ render status: NO_CONTENT }
        }
    }

    protected void notFound() {
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'photo.label', default: 'Photo'), params.id])
                redirect action: "index", method: "GET"
            }
            '*'{ render status: NOT_FOUND }
        }
    }
}

package safe

import grails.gorm.services.Service

@Service(Photo)
interface PhotoService {

    Photo get(Serializable id)

    List<Photo> list(Map args)

    Long count()

    void delete(Serializable id)

    Photo save(Photo photo)

    Photo save(String photoPath, String photoContentType, long createTime) // RLP adds

}
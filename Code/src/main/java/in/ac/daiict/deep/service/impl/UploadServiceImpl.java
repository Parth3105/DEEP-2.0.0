package in.ac.daiict.deep.service.impl;

import in.ac.daiict.deep.constant.response.ResponseMessage;
import in.ac.daiict.deep.constant.response.ResponseStatus;
import in.ac.daiict.deep.dto.ResponseDto;
import in.ac.daiict.deep.entity.Upload;
import in.ac.daiict.deep.repository.UploadRepo;
import in.ac.daiict.deep.service.UploadService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class UploadServiceImpl implements UploadService {
    private UploadRepo uploadRepo;

    @Override
    public ResponseDto insert(Upload file) {
        try{
            deleteFile(file.getName());
            uploadRepo.save(file);
            return new ResponseDto(ResponseStatus.OK,ResponseMessage.SUCCESS);
        } catch (Exception e){
            log.error("Insertion of {} failed with error: {}", file.getName(),e.getCause().getMessage(), e.getCause());
            return new ResponseDto(ResponseStatus.INTERNAL_SERVER_ERROR, ResponseMessage.UPLOAD_FAILURE);
        }
    }

    @Override
    public void deleteFile(String fileName) {
        uploadRepo.deleteById(fileName);
    }

    @Override
    public void insertAll(List<Upload> uploads) {
        uploadRepo.saveAllAndFlush(uploads);
    }

    @Override
    public Upload findFile(String name) {
        return uploadRepo.findById(name).orElse(null);
    }

    @Override
    public void deleteAll() {
        uploadRepo.deleteAll();
    }
}

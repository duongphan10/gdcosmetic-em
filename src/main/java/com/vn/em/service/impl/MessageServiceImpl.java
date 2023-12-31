package com.vn.em.service.impl;

import com.corundumstudio.socketio.SocketIOServer;
import com.vn.em.constant.CommonConstant;
import com.vn.em.constant.ErrorMessage;
import com.vn.em.constant.SortByDataConstant;
import com.vn.em.domain.dto.pagination.PaginationFullRequestDto;
import com.vn.em.domain.dto.pagination.PaginationResponseDto;
import com.vn.em.domain.dto.pagination.PagingMeta;
import com.vn.em.domain.dto.request.MessageRequestDto;
import com.vn.em.domain.dto.response.MessageDto;
import com.vn.em.domain.entity.File;
import com.vn.em.domain.entity.Message;
import com.vn.em.domain.entity.Room;
import com.vn.em.domain.entity.UserRoom;
import com.vn.em.domain.mapper.MessageMapper;
import com.vn.em.exception.NotFoundException;
import com.vn.em.repository.FileRepository;
import com.vn.em.repository.MessageRepository;
import com.vn.em.repository.RoomRepository;
import com.vn.em.repository.UserRoomRepository;
import com.vn.em.service.MessageService;
import com.vn.em.util.PaginationUtil;
import com.vn.em.util.UploadFileUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final FileRepository fileRepository;
    private final RoomRepository roomRepository;
    private final UserRoomRepository userRoomRepository;
    private final MessageMapper messageMapper;
    private final UploadFileUtil uploadFileUtil;
    private final SocketIOServer server;

    @Override
    public PaginationResponseDto<MessageDto> getAllByRoomId(Integer roomId, PaginationFullRequestDto paginationFullRequestDto) {
        roomRepository.findById(roomId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Room.ERR_NOT_FOUND_ID, new String[]{roomId.toString()}));
        Pageable pageable = PaginationUtil.buildPageable(paginationFullRequestDto, SortByDataConstant.MESSAGE);
        Page<Message> messagePage = messageRepository.getAllByRoomId(roomId, pageable);
        PagingMeta meta = PaginationUtil
                .buildPagingMeta(paginationFullRequestDto, SortByDataConstant.MESSAGE, messagePage);

        List<MessageDto> messageDtos = messageMapper.mapMessagesToMessageDtos(messagePage.getContent());

        return new PaginationResponseDto<>(meta, messageDtos);
    }

    @Override
    public MessageDto sendMessage(MessageRequestDto messageRequestDto) {
        Room room = roomRepository.findById(messageRequestDto.getRoomId())
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Room.ERR_NOT_FOUND_ID, new String[]{messageRequestDto.getRoomId().toString()}));
        Message message = messageMapper.mapMessageRequestDtoToMessage(messageRequestDto);
        message.setRoom(room);
        messageRepository.save(message);

        if (messageRequestDto.getFiles() != null && !messageRequestDto.getFiles().isEmpty()) {
            List<File> files = new ArrayList<>();
            for (MultipartFile multipartFile : messageRequestDto.getFiles()) {
                File file = new File();
                file.setPath(uploadFileUtil.uploadFile(multipartFile));
                file.setName(multipartFile.getOriginalFilename());
                file.setSize(multipartFile.getSize());
                file.setMessage(message);
                fileRepository.save(file);
                files.add(file);
            }
            message.setFiles(files);
        }
        MessageDto messageDto = messageMapper.mapMessageToMessageDto(messageRepository.save(message));

        List<UserRoom> userRooms = userRoomRepository.getAllUserByRoomId(room.getId());
        for (UserRoom userRoom : userRooms) {
            server.getRoomOperations(userRoom.getUser().getId().toString())
                    .sendEvent(CommonConstant.Event.SERVER_SEND_MESSAGE, messageDto);
        }
        return messageDto;
    }

}

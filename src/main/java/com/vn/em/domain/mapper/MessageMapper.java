package com.vn.em.domain.mapper;

import com.vn.em.constant.CommonConstant;
import com.vn.em.domain.dto.request.MessageRequestDto;
import com.vn.em.domain.dto.response.MessageDto;
import com.vn.em.domain.entity.Message;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface MessageMapper {

    @Mappings({
            @Mapping(target = "files", ignore = true),
    })
    Message mapMessageRequestDtoToMessage(MessageRequestDto messageRequestDto);

    @Mappings({
            @Mapping(target = "fileDtos", source = "files"),
            @Mapping(target = "roomId", source = "room.id"),
            @Mapping(target = "createdDate", source = "createdDate", dateFormat = CommonConstant.PATTERN_DATE_TIME),
            @Mapping(target = "lastModifiedDate", source = "lastModifiedDate", dateFormat = CommonConstant.PATTERN_DATE_TIME),
    })
    MessageDto mapMessageToMessageDto(Message message);

    List<MessageDto> mapMessagesToMessageDtos(List<Message> messages);

}

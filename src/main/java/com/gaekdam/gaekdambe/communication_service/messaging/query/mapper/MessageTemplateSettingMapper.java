package com.gaekdam.gaekdambe.communication_service.messaging.query.mapper;

import com.gaekdam.gaekdambe.communication_service.messaging.query.dto.response.MessageTemplateSettingFlatRow;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MessageTemplateSettingMapper {

    List<MessageTemplateSettingFlatRow> findSettingRows(Long propertyCode);
}

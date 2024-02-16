package com.rostyslav.trading.bot.mappers;

import com.rostyslav.trading.bot.dto.CandleWebClientResponse;
import com.rostyslav.trading.bot.model.ExtendedCandle;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface KlineMapper {

    KlineMapper mapperInstance = Mappers.getMapper(KlineMapper.class);

    ExtendedCandle dtoToDomain(CandleWebClientResponse response);
}

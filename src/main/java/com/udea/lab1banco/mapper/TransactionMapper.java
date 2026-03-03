package com.udea.lab1banco.mapper;

import com.udea.lab1banco.dto.TransactionDTO;
import com.udea.lab1banco.entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface TransactionMapper {
    TransactionMapper INSTANCE = Mappers.getMapper(TransactionMapper.class);
    TransactionDTO toDTO(Transaction transaction);
}
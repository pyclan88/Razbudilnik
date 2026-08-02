package com.ruslanataev.razbudilnik.data.reader.mappers

import com.ruslanataev.razbudilnik.data.reader.models.ReaderPageDto
import com.ruslanataev.razbudilnik.domain.reader.models.ReaderPage

object ReaderPageDtoToReaderPageMapper {

    fun map(readerPageDto: ReaderPageDto): ReaderPage {
        return ReaderPage(
            id = readerPageDto.id,
            text = readerPageDto.text,
        )
    }
}

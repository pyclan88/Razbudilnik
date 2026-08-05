package com.ruslanataev.razbudilnik.data.reader.mappers

import com.ruslanataev.razbudilnik.data.reader.models.ReaderBookDto
import com.ruslanataev.razbudilnik.domain.reader.models.ReaderBook

object ReaderBookDtoToReaderBookMapper {

    fun map(readerBookDto: ReaderBookDto): ReaderBook {
        return ReaderBook(
            id = readerBookDto.id,
            title = readerBookDto.title,
            author = readerBookDto.author,
            text = readerBookDto.text,
            pages = readerBookDto.pages.map(
                ReaderPageDtoToReaderPageMapper::map,
            ),
        )
    }
}

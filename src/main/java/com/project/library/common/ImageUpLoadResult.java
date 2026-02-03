package com.project.library.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageUpLoadResult {

    private String url;
    private String publicId;
    private Long size;
    private Integer width;
    private Integer height;
    private String format;
}

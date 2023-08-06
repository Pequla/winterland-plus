package com.pequla.winterland.model;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfigModel {
    private Integer id;
    private String key;
    private String value;
}

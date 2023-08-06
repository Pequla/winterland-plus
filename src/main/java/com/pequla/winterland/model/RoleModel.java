package com.pequla.winterland.model;

import lombok.*;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class RoleModel {

    private Integer id;
    private String discordId;
    private String name;
    private String group;
}

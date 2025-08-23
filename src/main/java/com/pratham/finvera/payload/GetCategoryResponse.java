package com.pratham.finvera.payload;

import java.util.List;

import com.pratham.finvera.entity.Category;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = false)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class GetCategoryResponse extends MessageResponse {

    private List<Category> category;
}

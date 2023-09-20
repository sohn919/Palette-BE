package fc.server.palette.purchase.dto.response;

import fc.server.palette.purchase.entity.type.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OfferListDto {
    private Long id;
    private String title;
    private Category category;
    private Integer price;
    private String thumbnailUrl;
    //todo
    private Integer bookmarkCount;
    private Integer hits;
}

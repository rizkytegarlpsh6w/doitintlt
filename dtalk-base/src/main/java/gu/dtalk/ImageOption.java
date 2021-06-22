package gu.dtalk;

import com.alibaba.fastjson.TypeReference;

import net.gdface.image.BaseLazyImage;

public class ImageOption extends BaseOption<BaseLazyImage> {

	public ImageOption() {
		super(new TypeReference<BaseLazyImage>() {}.getType());
	}

	@Override
	public OptionType getType() {
		return OptionType.IMAGE;
	}

}

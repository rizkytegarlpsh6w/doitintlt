package gu.dtalk;

import net.gdface.image.BaseLazyImage;

public class ImageOption extends BaseOption<BaseLazyImage> {

	@Override
	public OptionType getType() {
		return OptionType.IMAGE;
	}

	@Override
	public String toString(BaseLazyImage input) throws TransformException {
		if(null == input){
			throw new TransformException("NULL POINTER");
		}
		return null;
	}

	@Override
	public BaseLazyImage fromString(String input) throws TransformException {
		if(null == input){
			throw new TransformException("NULL POINTER");
		}
		return null;
	}

}

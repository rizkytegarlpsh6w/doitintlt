package gu.dtalk;

import java.io.IOException;

import net.gdface.image.BaseLazyImage;
import net.gdface.image.ImageErrorException;
import net.gdface.utils.FaceUtilits;

public class ImageOption extends BaseBinary {

	private volatile BaseLazyImage image;
	public ImageOption() {
	}

	@Override
	public OptionType getType() {
		return OptionType.IMAGE;
	}
	public <T>void writeValue(T src) throws IOException{
		setValue(FaceUtilits.getBytes(src));
	}
	public BaseLazyImage imageObj() throws ImageErrorException{
		if(image == null){
			synchronized (this) {
				if(image == null){
					image = BaseLazyImage.getLazyImageFactory().create(getValue());
				}
			}
		}
		return image;
	}
	public int getWidth(){
		try {
			return imageObj().getWidth();
		} catch (ImageErrorException e) {
			throw new IllegalArgumentException(e);
		}
	}
	public int getHeight(){
		try {
			return imageObj().getHeight();
		} catch (ImageErrorException e) {
			throw new IllegalArgumentException(e);
		}
	}
	public String getSuffix(){
		try {
			return imageObj().getSuffix();
		} catch (ImageErrorException e) {
			throw new IllegalArgumentException(e);
		}
	}


	
}

package gu.dtalk;

import java.io.IOException;

import com.google.common.base.Throwables;

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
	@Override
	public ImageOption asValue(String input) {
		try {
			setValue(FaceUtilits.getBytes(input));
			return this;
		} catch (Exception e) {
			Throwables.throwIfUnchecked(e);
			throw new RuntimeException(e);
		}
	}
	/**
	 * 从input中读取字节流转为byte[]调用{@link #setValue(byte[])}
	 * @param <T> 参见 {@link FaceUtilits#getBytes(Object)}
	 */
	public <T>ImageOption asValue(T input) {
		try {
			setValue(FaceUtilits.getBytes(input));
			return this;
		} catch (Exception e) {
			Throwables.throwIfUnchecked(e);
			throw new RuntimeException(e);
		}
	}
	@Override
	public ImageOption asDefaultValue(String input) {
		try {
			setDefaultValue(FaceUtilits.getBytes(input));
			return this;
		} catch (Exception e) {
			Throwables.throwIfUnchecked(e);
			throw new RuntimeException(e);
		}
	}
	/**
	 * 从input中读取字节流转为byte[]调用{@link #setDefaultValue(byte[])}
	 * @param <T> 参见 {@link FaceUtilits#getBytes(Object)}
	 */
	public <T>ImageOption asDefaultValue(T input) {
		try {
			setDefaultValue(FaceUtilits.getBytes(input));
			return this;
		} catch (Exception e) {
			Throwables.throwIfUnchecked(e);
			throw new RuntimeException(e);
		}
	}
}

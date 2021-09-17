package gu.dtalk;

import com.google.common.base.Throwables;

import net.gdface.image.ImageErrorException;
import net.gdface.image.LazyImage;
import net.gdface.utils.FaceUtilits;
import net.gdface.utils.Judge;

public class ImageOption extends BaseBinary {
	private volatile boolean updated = false;
	private LazyImage image;
	public ImageOption() {
	}

	@Override
	public OptionType getType() {
		return OptionType.IMAGE;
	}
	@Override
	public BaseOption<byte[]> setValue(byte[] value) {
		synchronized(this){
			super.setValue(value);
			updated = false;
		}
		return this;
	}

	/**
	 * 
	 * @return 返回图像对象,如果值为空则返回{@code null}
	 * @throws ImageErrorException
	 */
	public LazyImage imageObj() throws ImageErrorException{
		if(Judge.isEmpty(getValue())){
			return null;
		}
		if(!updated){
			synchronized (this) {
				if(!updated){
					image = LazyImage.create(getValue());
					updated = true;
				}
			}
		}
		
		return image;
	}
	/**
	 * 与{@link #imageObj()}类似，只是所有的异常都被封装到{@link RuntimeException}
	 * @return 返回图像对象,如果值为空则返回{@code null}
	 */
	public LazyImage imageObjUncheck(){
		try {
			return imageObj();
		} catch (Exception e) {
			Throwables.throwIfUnchecked(e);
			throw new RuntimeException(e);
		}
	}
	public int getWidth(){
		try{
			return imageObj().getWidth();
		} catch (Exception e) {
			return 0;
		}
	}
	public int getHeight(){
		try{
			return imageObj().getHeight();
		} catch (Exception e) {
			return 0;
		}
	}
	public String getSuffix() throws ImageErrorException{
		try{
			return imageObj().getSuffix();
		} catch (Exception e) {
			return null;
		}
	}
	/**
	 * 从input(Base64格式)中解码为byte[]调用{@link #setDefaultValue(byte[])}
	 * @see gu.dtalk.BaseOption#asValue(java.lang.String)
	 */
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
		super.asDefaultValue(input);
		return this;
	}
	/**
	 * 从input(Base64格式)中解码为byte[]调用{@link #setDefaultValue(byte[])}
	 * @see gu.dtalk.BaseOption#asDefaultValue(java.lang.String)
	 */
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
	 * 从input中读取字节流转为byte[]调用{@link #setValue(byte[])}
	 * @param <T> 参见 {@link FaceUtilits#getBytes(Object)}
	 */
	public <T>ImageOption asDefaultValue(T input) {
		super.asDefaultValue(input);
		return this;
	}
	/**
	 * 从input中读取字节流转为byte[]调用{@link #setDefaultValue(byte[])}
	 * @param <T> 参见 {@link FaceUtilits#getBytes(Object)}
	 */
	public <T>ImageOption asValue(LazyImage input) {
		setValue(input.wirteJPEGBytes());
		this.image = input;
		return this;
	}
	/**
	 * 从input中读取字节流转为byte[]调用{@link #setDefaultValue(byte[])}
	 * @param <T> 参见 {@link FaceUtilits#getBytes(Object)}
	 */
	public <T>ImageOption asDefaultValue(LazyImage input) {
		setDefaultValue(input.wirteJPEGBytes());
		this.image = input;
		return this;
	}
}

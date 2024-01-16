package gu.dtalk;

import com.google.common.base.MoreObjects;
import com.google.common.base.Throwables;

import net.gdface.image.ImageErrorException;
import net.gdface.image.BaseLazyImage;
import net.gdface.utils.BinaryUtils;
import net.gdface.utils.Judge;

/**
 * 图像类型选项
 * @author guyadong
 *
 */
public class ImageOption extends BaseBinary {
	private volatile boolean updated = false;
	private BaseLazyImage image;
	public ImageOption() {
	}

	@Override
	public OptionType getType() {
		return OptionType.IMAGE;
	}
	@Override
	public ImageOption setValue(byte[] value) {
		synchronized(this){
			super.setValue(value);
			updated = false;
		}
		return this;
	}

	/**
	 * 
	 * @return 返回图像对象,如果值为空则返回{@code null}
	 * @throws ImageErrorException 图像读取异常
	 */
	public BaseLazyImage imageObj() throws ImageErrorException{
		if(Judge.isEmpty(getValue())){
			return null;
		}
		if(!updated){
			synchronized (this) {
				if(!updated){
					image = BaseLazyImage.getLazyImageFactory().create(getValue());
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
	public BaseLazyImage imageObjUncheck(){
		try {
			return imageObj();
		} catch (Throwable e) {
			Throwables.throwIfUnchecked(e);
			throw new RuntimeException(e);
		}
	}
	public int getWidth(){
		try{
			return imageObj().getWidth();
		} catch (Throwable e) {
			return 0;
		}
	}
	public int getHeight(){
		try{
			return imageObj().getHeight();
		} catch (Throwable e) {
			return 0;
		}
	}
	public String getSuffix() throws ImageErrorException{
		try{
			return imageObj().getSuffix();
		} catch (Throwable e) {
			return null;
		}
	}
	/**
	 * 从input(Base64格式)中解码为byte[]调用{@link #setDefaultValue(Object)}
	 * @see gu.dtalk.BaseOption#asValue(java.lang.String)
	 */
	@Override
	public ImageOption asValue(String input) {
		try {
			setValue(BinaryUtils.getBytes(input));
			return this;
		} catch (Throwable e) {
			Throwables.throwIfUnchecked(e);
			throw new RuntimeException(e);
		}
	}
	/**
	 * 从input中读取字节流转为byte[]调用{@link #setValue(byte[])}
	 * @param <T> 参见 {@link BinaryUtils#getBytes(Object)}
	 */
	public <T>ImageOption asValue(T input) {
		super.asDefaultValue(input);
		return this;
	}
	/**
	 * 从input(Base64格式)中解码为byte[]调用{@link #setDefaultValue(Object)}
	 * @see gu.dtalk.BaseOption#asDefaultValue(java.lang.String)
	 * @param input base64格式输入图像
	 * @return 当前对象
	 */
	@Override
	public ImageOption asDefaultValue(String input) {
		try {
			setDefaultValue(BinaryUtils.getBytes(input));
			return this;
		} catch (Throwable e) {
			Throwables.throwIfUnchecked(e);
			throw new RuntimeException(e);
		}
	}
	/**
	 * 从input中读取字节流转为byte[]调用{@link #setValue(byte[])}
	 * @param <T> 参见 {@link BinaryUtils#getBytes(Object)}
	 * @param input 图像数据
	 * @return 当前对象
	 */
	public <T>ImageOption asDefaultValue(T input) {
		super.asDefaultValue(input);
		return this;
	}
	/**
	 * 从input中读取字节流转为byte[]调用{@link #setDefaultValue(Object)}
	 * @param <T> 参见 {@link BinaryUtils#getBytes(Object)}
	 * @param input 输入图像
	 * @return 当前对象
	 */
	public <T>ImageOption asValue(BaseLazyImage input) {
		setValue(input.wirteJPEGBytes());
		this.image = input;
		return this;
	}
	/**
	 * 从input中读取字节流转为byte[]调用{@link #setDefaultValue(Object)}
	 * @param <T> 参见 {@link BinaryUtils#getBytes(Object)}
	 * @param input 输入图像
	 * @return 当前对象
	 */
	public <T>ImageOption asDefaultValue(BaseLazyImage input) {
		setDefaultValue(input.wirteJPEGBytes());
		this.image = input;
		return this;
	}

	@Override
	public String contentOfValue() {
		try {
			BaseLazyImage img = imageObj();
			if(img != null){
				return String.format("%s(%dx%d),size=%d", img.getSuffix(),img.getWidth(),img.getHeight(),getValue().length);
			}
		} catch (ImageErrorException e) {
			return MoreObjects.firstNonNull(e.getMessage(),"NOT IMAGE");
		}
		
		return super.contentOfValue();
	}
}

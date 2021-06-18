package gu.dtalk;

public abstract class BaseNumOption<T extends Number> extends BaseOption<T> {

	@Override
	public String toString(T input) throws TransformException {
		if(null == input){
			throw new TransformException("NULL POINTER");
		}
		return input.toString();
	}


}

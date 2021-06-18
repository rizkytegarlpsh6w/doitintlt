package gu.dtalk;

public interface StringTransformer<T> {
	String toString(T input) throws TransformException;
	T fromString(String input) throws TransformException;
}

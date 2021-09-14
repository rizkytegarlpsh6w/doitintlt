package gu.dtalk;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import static com.google.common.base.Preconditions.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;

/**
 * 数值验证器
 * @author guyadong
 *
 * @param <T> 数据类型,必须{@link Number}的子类
 */
public class NumberValidator<T> implements Predicate<T> {
	public static final NumberValidator<Object> DEFAULT_VALIDATOR = new NumberValidator<Object>();
	public static final NumberValidator<Object> NORMALIZATION_VALIDATOR = new NumberValidator<Object>((Double)0.0,true,(Double)1.0,true);
	private T min = null, max = null;
	private LinkedHashSet<T> values = null;
	
	private Comparator<T> numCcomparator = new Comparator<T>(){

		@Override
		public int compare(T o1, T o2) {
			if((o1 instanceof Integer) && (o2 instanceof Integer) ){
				return ((Integer)o1).compareTo((Integer)o2);
			}
			if((o1 instanceof Short) && (o2 instanceof Short) ){
				return ((Short)o1).compareTo((Short)o2);
			}
			if((o1 instanceof Byte) && (o2 instanceof Byte) ){
				return ((Byte)o1).compareTo((Byte)o2);
			}
			if((o1 instanceof Float) && (o2 instanceof Float) ){
				return ((Float)o1).compareTo((Float)o2);
			}
			if((o1 instanceof Float) && (o2 instanceof Float) ){
				return ((Float)o1).compareTo((Float)o2);
			}
			if((o1 instanceof Double) && (o2 instanceof Double) ){
				return ((Double)o1).compareTo((Double)o2);
			}
			throw new IllegalArgumentException("UNSUPPORTED TYPE");
		}};
	private boolean mineq;
	private boolean maxeq;
	public NumberValidator() {
		this(null,true,null,true);
	}
	public NumberValidator(T min,boolean mineq,T max,boolean maxeq) {
		this.min = min;
		this.max = max;
		this.mineq = mineq;
		this.maxeq = maxeq;
		checkArgument(min == null || Number.class.isAssignableFrom(min.getClass()));
		checkArgument(max == null || Number.class.isAssignableFrom(max.getClass()));
		if(min != null && max !=null){
			checkArgument(numCcomparator.compare(min, max) < 0,"value of max must less than min");
		}
	}
	@SafeVarargs
	public NumberValidator(T ... values) {
		this.values = Sets.newLinkedHashSet(Iterables.filter(Arrays.asList(checkNotNull(values)),Predicates.notNull()));
		checkArgument(!this.values.isEmpty(),"EMPTY LIST FOR values");
		Iterables.any(this.values, new Predicate<T>() {
			@Override
			public boolean apply(T input) {
				checkArgument(input instanceof Number,"INVALID type of values");
				return true;
			}
		});
	}
	@Override
	public boolean apply(T input) {
		if(input == null){
			return false;
		}
		if(values != null){
			return values.contains(input);
		}else{
			boolean check = true;
			if(min != null){
				int comp = numCcomparator.compare(min, input);
				check = check && ((mineq && comp <= 0 ) || comp <0);
			}
			if(max != null){
				int comp = numCcomparator.compare(input,max);
				check = check && ((maxeq && comp >= 0 ) || comp >0);
			}
			return check;
		}
	}
	@Override
	public String toString() {
		final int maxLen = 10;
		StringBuilder builder = new StringBuilder();
		builder.append("NumberValidator [");
		if (min != null) {
			builder.append("min=");
			builder.append(min);
			builder.append(", ");
		}
		if (max != null) {
			builder.append("max=");
			builder.append(max);
			builder.append(", ");
		}
		if (values != null) {
			builder.append("values=");
			builder.append(toString(values, maxLen));
			builder.append(", ");
		}
		if (numCcomparator != null) {
			builder.append("numCcomparator=");
			builder.append(numCcomparator);
		}
		builder.append("]");
		return builder.toString();
	}
	private String toString(Collection<?> collection, int maxLen) {
		StringBuilder builder = new StringBuilder();
		builder.append("[");
		int i = 0;
		for (Iterator<?> iterator = collection.iterator(); iterator.hasNext() && i < maxLen; i++) {
			if (i > 0)
				builder.append(", ");
			builder.append(iterator.next());
		}
		builder.append("]");
		return builder.toString();
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@SafeVarargs
	public static final <T>Predicate<Object> makeValidator(T ...values){
		checkArgument(values != null && values.length >1);
		return new NumberValidator(values);
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static final <T>Predicate<Object> makeValidator( T min,boolean mineq,T max,boolean maxeq){
		return new NumberValidator(min,mineq,max,maxeq);
	}
}

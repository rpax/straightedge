/*
 * Copyright (c) 2008, Keith Woodward
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of Keith Woodward nor the names
 *    of its contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 */
package straightedge.geom.util;

import java.util.*;

/**
 * Call it an unordered list or a multiset, this collection is defined
 * by oxymorons. Find the original <a
 * href=http://riven8192.blogspot.com/
 * 2009/08/bag-unordered-list-fast-remove.html>over here</a>
 *
 * @author Riven
 * @param <T>
 */
public class Bag<T> implements Collection<T>{

	public T[] data;
	public int size;

	/***/
	public Bag() {
		this(4);
	}

	/**
	 * @param space
	 *           initial size of bag
	 */
	@SuppressWarnings("unchecked")
	public Bag(int space) {
		this.data = (T[]) new Object[space];
	}

	/**
	 * Add an element
	 *
	 * @param t
	 */
	public boolean add(T t) {
		//data = ensureCapacity(data, size + 1, 1.75f);
		ensureCapacity(size + 1, 1.75f);
		data[size++] = t;
		return true;
	}

	/**
	 * Adds a lot of elements
	 *
	 * @param bag
	 */
	public void addAll(Bag<T> bag) {
		if (bag.size == 0) {
			return;
		}

		//data = ensureCapacity(data, this.size + bag.size, 1.75f);
		ensureCapacity(this.size + bag.size, 1.75f);
		System.arraycopy(bag.data, 0, this.data, this.size, bag.size);
		this.size += bag.size;
	}

	public void addAll(Object[] array) {
		if (array.length == 0) {
			return;
		}

		//data = ensureCapacity(data, this.size + array.length, 1.75f);
		ensureCapacity(this.size + array.length, 1.75f);
		System.arraycopy(array, 0, this.data, this.size, array.length);
		this.size += array.length;
	}

	/**
	 * Retrieves an element
	 *
	 * @param index
	 * @return the indexed element
	 */
	public T get(int index) {
		if (index >= size) {
			throw new ArrayIndexOutOfBoundsException();
		}
		return data[index];
	}

	/**
	 * Retrieves and removes an element
	 *
	 * @param index
	 * @return The indexed element
	 */
	public T remove(int index) {
		if (index >= size) {
			throw new ArrayIndexOutOfBoundsException();
		}

		T took = data[index];
		data[index] = data[ --
		size ]  ;
		data[ size ] = null;
		return took;
	}

	/**
	 * Attempts to remove an element
	 *
	 * @param t
	 *           The element to remove
	 * @return t
	 */
	public boolean remove(Object t) {
		int i = this.indexOf(t);
		if (i == -1) {
			return false;
		}
		this.remove(i);
		return true;
	}

	/**
	 * Tests if an element is present
	 *
	 * @param t
	 * @return <code>true</code> if t is present, <code>false</code>
	 *         otherwise
	 */
	public boolean contains(Object t) {
		return this.indexOf(t) != -1;
	}

	/**
	 * Finds the index of an element
	 *
	 * @param t
	 * @return the index of t, or -1 if not found
	 */
	public int indexOf(Object t) {
		for (int i = 0; i < size; i++) {
			if (data[i] == t) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Tries to shrink the internal array. If your {@link Bag} holds a
	 * lot of elements temporarily and you're concerned with memory
	 * usage, it might be worthwhile to call this after a sequence of
	 * {@link #take(int)} calls
	 */
	@SuppressWarnings("unchecked")
	public void shrink() {
		if (this.data.length > 8) {
			int factor = 4;

			if (this.size < this.data.length / factor) {
				int newSize = Math.max(4, this.size);
				T[] newData = (T[]) new Object[newSize];
				System.arraycopy(this.data, 0, newData, 0, this.size);
				this.data = newData;
			}
		}
	}

	/**
	 * Removes all elements
	 */
	public void clear() {
		for (int i = 0; i < size; i++) {
			data[i] = null;
		}
		this.size = 0;
	}

	/**
	 * The maximum number of elements allowed before the internal array
	 * will be grown
	 *
	 * @return The size of the internal array
	 */
	public int capacity() {
		return this.data.length;
	}

	/**
	 * @return The number of held elements
	 */
	public int size() {
		return size;
	}

	/**
	 * @return A new array of the data.
	 */
	public Object[] toArray() {
        return Arrays.copyOf(data, size);
    }
	
    public <T> T[] toArray(T[] a) {
        if (a.length < size){
            // Make a new array of a's runtime type, but my contents:
            return (T[]) Arrays.copyOf(data, size, a.getClass());
		}
		System.arraycopy(data, 0, a, 0, size);
		if (a.length > size){
			a[size] = null;
		}
		return a;
    }

//	@SuppressWarnings("unchecked")
//	private final void ensureCapacity(int minCapacity, float factor) {
//		if (data.length >= minCapacity) {
//			return;
//		}
//		int newCapacity = src.length + 1;
//		do {
//			newCapacity *= factor;
//		} while (newCapacity < minCapacity);
//
//		T[] dst = (T[]) new Object[newCapacity];
//		System.arraycopy(src, 0, dst, 0, src.length);
//		return dst;
//	}
	public void ensureCapacity(int minCapacity, float factor) {
		int oldCapacity = data.length;
		if (minCapacity > oldCapacity) {
			//int newCapacity = (oldCapacity * 3) / 2 + 1;
			int newCapacity = (int)(oldCapacity * factor) + 1;
			if (newCapacity < minCapacity) {
				newCapacity = minCapacity;
			}
			// minCapacity is usually close to size, so this is a win:
			data = Arrays.copyOf(data, newCapacity);
		}
	}

//	@SuppressWarnings("unchecked")
//	private static final <T> T[] ensureCapacity(T[] src, int minCapacity, float factor) {
//		if (src.length >= minCapacity) {
//			return src;
//		}
//		int newCapacity = src.length + 1;
//		do {
//			newCapacity *= factor;
//		} while (newCapacity < minCapacity);
//
//		T[] dst = (T[]) new Object[newCapacity];
//		System.arraycopy(src, 0, dst, 0, src.length);
//		return dst;
//	}


	public boolean addAll(Collection<? extends T> c) {
		Object[] a = c.toArray();
        int numNew = a.length;
		//data = ensureCapacity(data, size + numNew, 1.75f);
		ensureCapacity(size + numNew, 1.75f);
        System.arraycopy(a, 0, data, size, numNew);
        size += numNew;
		return numNew != 0;
    }

	public boolean removeAll(Collection<?> c) {
		boolean modified = false;
		Iterator<?> e = iterator();
		while (e.hasNext()) {
			if (c.contains(e.next())) {
			e.remove();
			modified = true;
			}
		}
		return modified;
    }

	public boolean containsAll(Collection<?> c) {
		Iterator<?> e = c.iterator();
		while (e.hasNext()){
			if (contains(e.next()) == false){
				return false;
			}
		}
		return true;
    }
	public boolean isEmpty() {
		return size == 0;
    }

	public boolean retainAll(Collection<?> c) {
		boolean modified = false;
		Iterator<T> e = iterator();
		while (e.hasNext()) {
			if (!c.contains(e.next())) {
				e.remove();
				modified = true;
			}
		}
		return modified;
    }

	public Iterator<T> iterator() {
		return new Itr();
    }

	private class Itr implements Iterator<T> {
		/**
		 * Index of element to be returned by subsequent call to next.
		 */
		int cursor = 0;

		/**
		 * Index of element returned by most recent call to next or
		 * previous.  Reset to -1 if this element is deleted by a call
		 * to remove.
		 */
		int lastRet = -1;

		public boolean hasNext() {
				return cursor != size();
		}

		public T next() {
				checkForComodification();
			try {
			T next = get(cursor);
			lastRet = cursor++;
			return next;
			} catch (IndexOutOfBoundsException e) {
			checkForComodification();
			throw new NoSuchElementException();
			}
		}

		public void remove() {
			if (lastRet == -1)
			throw new IllegalStateException();
				checkForComodification();

			try {
			Bag.this.remove(lastRet);
			if (lastRet < cursor)
				cursor--;
			lastRet = -1;
			} catch (IndexOutOfBoundsException e) {
			throw new ConcurrentModificationException();
			}
		}

		final void checkForComodification() {
		}
    }
}

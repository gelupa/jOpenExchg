/* Java Open Exchange(jOpenExchg) Project
 *
 * Copyright (C) 2013  Alex Song
 *
 * This file is part of jOpenExchg.  
 *
 * jOpenExchg is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * jOpenExchg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General
 * Public License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 */
package org.jopenexchg.pool;

import java.lang.reflect.Array;

public final class AllocOnlyPool<T> 
{
	private static int MIN_POOL_SIZE = 100;
	
	// The following 2 fields will be valid when init() once
	private T slotArray[] = null;
	private int usedCnt = 0;
	private int poolSize = 0;
	
	@SuppressWarnings("unchecked")
	public AllocOnlyPool(Class<T> elemType, int size)
		throws InstantiationException, IllegalAccessException
	{
		if(size <= MIN_POOL_SIZE)
		{
			size = MIN_POOL_SIZE;
		}
		
		//
		// Another way to create T[] in Generic Class
		//	 	T[] array = (T[])(new ArrayList<T>(size).toArray());
		//
		slotArray = (T[])(Array.newInstance(elemType, size));
	
		for(int i = 0; i < size; i++)
		{
			T item = elemType.newInstance();
			slotArray[i] = item;
		} 
		
		poolSize = size;
	}

	/**
	 * 最大容量
	 * @return
	 */
	public final int capacity()
	{
		return poolSize;
	}

	// 从池子里面请求一个对象。如果没有对象可以拿，返回 null
	public final T getObj()
	{
		if(usedCnt >= poolSize)
		{
			return null;
		}
		else
		{
			usedCnt = usedCnt + 1;
			return slotArray[usedCnt];
		}
	}	
	
	/**
	 * 已分配出去的数量
	 * @return
	 */
	public final int size()
	{
		return usedCnt;

	}

	public final void finalize()
	{
		slotArray = null;
	}
}

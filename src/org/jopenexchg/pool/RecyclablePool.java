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
import java.util.LinkedList;


/**
 * 
 * 这个类并不是线程安全的
 * 
 * int 的最大值是 2,147,483,648 = 21亿。对于交易系统的POOL应该够了
 * 
 */
public final class RecyclablePool<T extends WithId> 
{
	private static int MIN_POOL_SIZE = 100;
	
	// The following 2 fields will be valid when init() once
	private T slotArray[] = null;
	private boolean useflagArray[] = null;
	private LinkedList<T> freeList = null;
	private long poolSize = 0;
	
	@SuppressWarnings("unchecked")
	public RecyclablePool(Class<T> elemType, int size)
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
		useflagArray = new boolean[size];
		freeList = new LinkedList<T>();
	
		for(int i = 0; i < size; i++)
		{
			T item = elemType.newInstance();
			item.setId(i);			
			
			slotArray[i] = item;
			useflagArray[i] = false;
			freeList.add(item);				
		} 
		
		poolSize = size;
	}

	/**
	 * 最大容量
	 * @return
	 */
	public final int capacity()
	{
		return slotArray.length;
	}

	// 从池子里面请求一个对象。如果没有对象可以拿，返回 null
	public final T getObj()
	{
		if(freeList.size() > 0)
		{
			T obj = freeList.poll();
			if(obj != null)
			{
				int id = obj.getId();
				// mark as used
				useflagArray[id] = true;
			}
			return obj;
		}
		else
		{
			return null;
		}
	}	
	
	// 向池中返还一个对象。可以重复返还
	public final void putObj(T obj)
	{
		if(obj != null)
		{
			int id = obj.getId();
			
			if((0 <= id) && (id < poolSize))
			{
				if( useflagArray[id] == true)
				{
					freeList.add(obj);
					useflagArray[id] = false;
				}
			}
		}

	}

	/**
	 * 根据 id 来查找一个已分配的对象
	 * 
	 * @param id: slotId，也就是数组下标
	 * @return: T when such id is really used. Otherwise null
	 */
	public final T findUsedObj(int id)
	{
		if((0 <= id) && (id < useflagArray.length))
		{
			if( useflagArray[id] == true)
			{
				return slotArray[id];
			}			
		}		

		return null;
	}
	
	/**
	 * 已分配出去的数量
	 * @return
	 */
	public final int size()
	{
		return slotArray.length - freeList.size();

	}

	public final void finalize()
	{
		slotArray = null;
		useflagArray = null;
		freeList = null;
	}
}



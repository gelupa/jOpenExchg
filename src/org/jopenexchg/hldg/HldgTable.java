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
package org.jopenexchg.hldg;

import java.util.*;

import org.jopenexchg.pool.*;

/**
 * 
 * 持仓表建立后就不清表项.
 *
 */
public final class HldgTable
{
	static final int DEF_HLDG_TABLE_SIZE = 1000;
	
	private AllocOnlyPool<Hldg> hldgPool = null;
	private HashMap<HldgKey, Hldg> hldgTbl = null;
	
	public HldgTable(int maxSize) 
		throws InstantiationException, IllegalAccessException
	{
		if(maxSize <= 0)
		{
			maxSize = DEF_HLDG_TABLE_SIZE;
		}
	
		hldgPool = new AllocOnlyPool<Hldg>(Hldg.class, maxSize);
		hldgTbl = new HashMap<HldgKey, Hldg>(maxSize);
	}
	
	/**
	 * 
	 * @param key
	 * @return 如果表里面没有对应记录，会返回 null，不会创建记录
	 */
	public final Hldg findHldg(HldgKey key)
	{
		if(key == null)
		{
			return null;
		}
		
		return hldgTbl.get(key);
	}
	
	public final void putHldg(Hldg hldg)
	{
		if(hldg != null && hldg.key != null)
		{
			hldgTbl.put(hldg.key, hldg);
		}
	}
	
	/**
	 * 
	 * @param key
	 * @return 总是会返回一条记录。如果没有就会新增一条对应Key的空记录
	 */
	public final Hldg getHldg(HldgKey key)
	{
		if(key == null)
		{
			return null;
		}
		
		Hldg hldg = hldgTbl.get(key);
		if(hldg == null)
		{
			hldg = hldgPool.getObj();
			if(hldg == null)
			{
				return null;
			}
			hldg.key = key;
			return hldg;
		}
		else
		{
			return hldg;
		}
	}
	
}

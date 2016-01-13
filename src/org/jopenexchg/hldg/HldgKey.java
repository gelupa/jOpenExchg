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

public final class HldgKey
{
	/**
	 *  FIVE ELEMENTS IN ALL
	 */
	public byte accType;
	public int accNo;
	
	public int stockid;
	
	public short pbu;
	public short hldgType;
	
	/**
	 *  在测试代码中：
	 *  随机产生持仓：
	 *  	原生类型  1000 * 1000 次随机产生持仓使用 110ms
	 *  	非原生类型 1000 * 1000 次随机产生持仓使用 140ms
	 *  hashCode 构建的速度：
	 *		非原生类型hashCode相加: 142ms - 140ms = 2ms, 可见开销主要在随机产生数据上
	 *		如果使用原生类型并利用移位等产生long型再返回hashCode, 会使用142ms - 110ms = 42ms
	 *		如果使用原生类型并直接相加字段值后返回int，总耗时是 (110ms - 110ms), 几乎不耗时
	 *  总的hash表 访问速度(里面包含了hashCode构建速度)；
	 *  	原生类型类型，移位法产生long后返回其hashCode: 267ms - 110ms = 157ms
	 *      原生类型，直接字段相加返回法: 235ms - 110ms = 125 ms
	 *  结论：
	 *  	HldgKey建议使用原生类型, hash值可以采用最简单的方式来产生, 总体性能也不差             
	 */	
	public final int hashCode()
	{
		return accType + accNo + stockid + pbu + hldgType;
	}
	
	/**
	 * 需要自己撰写 equals() 以覆盖缺省实现： this == obj 
	 * 
	 * @param obj
	 * @return
	 */
	public final boolean equals(HldgKey obj)
	{
		if(obj == null)
		{
			return false;
		}
		
		if (this == obj)
		{
			return true;
		}
		
		if(this.accType != obj.accType)
		{
			return false;
		}
		
		if(this.accNo != obj.accNo)
		{
			return false;
		}
		
		if(this.stockid != obj.stockid)
		{
			return false;
		}
		
		if(this.pbu != obj.pbu)
		{
			return false;
		}
		
		if(this.hldgType != obj.hldgType)
		{
			return false;
		}
		
		return true;
	}
	
}
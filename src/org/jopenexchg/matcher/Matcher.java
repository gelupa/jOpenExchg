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
 * 
 * 
 * Modify History:
 * 2014-02-22   Alex Song   
 * 		Modify the open auction algorithm to reflect the correct 
 * 		understanding of the phrase "the remain quantity should be minimal"
 * 
 * 2014-05-19   Alex Song
 *      Implemented a newly found simple algorithm for open auction
 */
package org.jopenexchg.matcher;

import java.util.*;

import org.jopenexchg.matcher.biz.BizAdaptor;
import org.jopenexchg.matcher.event.*;
import org.jopenexchg.pool.*;


public final class Matcher implements BizAdaptor
{
	private TradedInstList stockList = null;
	private RecyclablePool<PriceLeader> prcLdrPool = null;
	private AllocOnlyPool<Order> ordrPool = null;
	private EventHandler evtCbs = null;
	private BizAdaptor bizAdpt = null;
	private LinkedList<Long> delPrcLdrList = null;
	private LinkedList<Long> delPrcLdrList2 = null;
	
	public Matcher(int prcLdrCnt, int orderCnt) 
		throws InstantiationException, IllegalAccessException
	{
		bizAdpt = this;
		stockList = new TradedInstList();
		prcLdrPool =  new RecyclablePool<PriceLeader>(PriceLeader.class, prcLdrCnt);
		ordrPool = new AllocOnlyPool<Order>(Order.class, orderCnt);
		delPrcLdrList = new LinkedList<Long>();
		delPrcLdrList2 = new LinkedList<Long>();
		
	}

	public final void setBizAdpt(BizAdaptor bizAdpt) 
	{
		if(bizAdpt != null)
		{
			this.bizAdpt = bizAdpt;
		}
	}	
	
	public final void setEvtCbs(EventHandler evtCbs)
	{
		this.evtCbs = evtCbs;
	}
	
	public final TradedInst addStock(int stockId, String stockName)
	{
		TradedInst stock = new TradedInst(stockId, stockName);
		
		stockList.addStock(stock);
		
		return stock;
	}
	
	public final Order allocOrder()
	{
		return ordrPool.getObj();
	}

	/**
	 *  用来根据slotId来撤单
	 *  
	 * @param id
	 * @return
	 */
	public final boolean delOrder(Order order)
	{
		if(order == null)
		{
			return false;
		}
		
		if(order.stock == null)
		{
			order.stock = stockList.getStock(order.stockid);
			if(order.stock == null)
			{
				return false;
			}
		}
		
		order.delflg = true;
		long prior = bizAdpt.calcPrior(order);

		TreeMap<Long, PriceLeader> prcList = order.stock.getPrcList(order.isbuy);
		PriceLeader prcLdr = prcList.get(prior);
		if(prcLdr == null)
		{
			return false;
		}
		
		prcLdr.accumQty -= order.remQty;
		if(prcLdr.accumQty <= 0)
		{
			prcList.remove(prcLdr.prior);
			prcLdrPool.putObj(prcLdr);
		}

		if(this.evtCbs != null)
		{
			evtCbs.leaveOrderBook(order);
		}	
		
		return true;
	}
	
	// 简单插入订单簿. 可能增加价格档位，增加已存在的价格档位的累积数量，订单簿增加订单
	private final boolean insertOrder(Order order)
	{
		if(order.remQty <= 0)
		{
			return false;
		}
		
		if(order.stock == null)
		{
			order.stock = stockList.getStock(order.stockid);
			if(order.stock == null)
			{
				return false;
			}			
		}
		
		long prior = bizAdpt.calcPrior(order);
		
		PriceLeader prcLdr = order.stock.getPrcList(order.isbuy).get(prior);
		
		if(prcLdr == null)
		{
			prcLdr = prcLdrPool.getObj();
			if(prcLdr == null)
			{
				return false;
			}
			
			prcLdr.prior = prior;
			prcLdr.price = order.price;
			prcLdr.ordPrc = order.ordPrc;
			
			order.stock.addtoPrcList(order.isbuy, prcLdr);
		}
		
		prcLdr.orderList.add(order);
		
		// 只有在加入单子的时候这个量才上升
		prcLdr.accumQty += order.remQty;
		
		if(this.evtCbs != null)
		{
			evtCbs.enterOrderBook(order);
		}		
		
		return true;
	}
	
	// 在这个里面会减少对手方  prcLdr.accumQty
	private final boolean matchOnePrcLvl(Order newOrd, PriceLeader prcLdr, TreeMap<Long, PriceLeader> peerPrcLdrTree, TradedInst stock)
	{
		Order oldOrd = null;
		long matchQty = 0;
		
		while(newOrd.remQty > 0)
		{
			oldOrd = prcLdr.orderList.peek();
			if(oldOrd == null)
			{
				return true;
			}
			
			if(oldOrd.delflg == true)
			{
				// delayed deleting of deleted orders from list
				prcLdr.orderList.remove();	
			}
			else
			{
				if(oldOrd.remQty <= newOrd.remQty)
				{
					matchQty = oldOrd.remQty;
					newOrd.remQty -= matchQty;
					oldOrd.remQty = 0;
					
					prcLdr.accumQty -= matchQty;
					prcLdr.orderList.remove();
					
					if(this.evtCbs != null)
					{
						evtCbs.match(newOrd, oldOrd, matchQty, prcLdr.price);
						
						evtCbs.leaveOrderBook(oldOrd);
					}						
				}
				else
				{
					matchQty = newOrd.remQty;
					oldOrd.remQty -= matchQty;
					newOrd.remQty = 0;
					
					prcLdr.accumQty -= matchQty;
					
					if(this.evtCbs != null)
					{
						evtCbs.match(newOrd, oldOrd, matchQty, prcLdr.price);
					}						
				}
			}

		}
		
		return true;
	}
	
	// 集合竞价期间插入订单簿，不做MATCH
	public final boolean ocallInsOrder(Order order)
	{
		if(order.stock == null)
		{
			order.stock = stockList.getStock(order.stockid);
			if(order.stock == null)
			{
				System.out.println("getStock() failed");
				return false;
			}
		}	
		order.remQty = order.ordQty;
		order.price = bizAdpt.ordPrc2Price(order.ordPrc);
		
		if(evtCbs != null)
		{
			evtCbs.incomingOrder(order);
		}		
		return insertOrder(order);
	}
	
	
	// 这个是连续竞价时候使用的方式，先尝试匹配再插入订单簿
	public final boolean matchInsOrder(Order order)
	{
		order.remQty = order.ordQty;
		order.price = bizAdpt.ordPrc2Price(order.ordPrc);

		if(order.stock == null)
		{
			order.stock = stockList.getStock(order.stockid);
			if(order.stock == null)
			{
				System.out.println("getStock() failed");
				return false;
			}
		}
		
		if(evtCbs != null)
		{
			evtCbs.incomingOrder(order);
		}
		
		long maxPeerPrior = bizAdpt.calcMaxPrior(!(order.isbuy), order.price);

		TreeMap<Long, PriceLeader> peerPrcLdrTree = order.stock.getPeerPrcTree(order.isbuy);
		Set<Map.Entry<Long, PriceLeader> > peerPrcLdrSet = peerPrcLdrTree.entrySet();
		
		long priceLevelCnt = 0;
		long prevPrice = TradedInst.NO_PRICE;
		
		Map.Entry<Long, PriceLeader> peerEntry = null;
		PriceLeader prcLdr = null;
		
		delPrcLdrList.clear();
		
		Iterator<Map.Entry<Long, PriceLeader>> its =  peerPrcLdrSet.iterator();
		while(its.hasNext() && (order.remQty > 0))
		{
			peerEntry = its.next();
			prcLdr = peerEntry.getValue();
			
			if(prcLdr.prior <= maxPeerPrior)
			{
				if(prevPrice != prcLdr.price)
				{
					priceLevelCnt++;
					prevPrice = prcLdr.price;
					
					// 在这里以后可以利用priceLevelCnt来控制市价订单吃多少档位
					if(priceLevelCnt >= 5)
					{
					}
				}
				
				// 针对此档位上的订单列表进行匹配
				if(false == matchOnePrcLvl(order, prcLdr, peerPrcLdrTree, order.stock))
				{
					System.out.println("matchOnePrcLvl() failed");
					return false;
				}
				
				// 放入待回收价格档位列表
				if(prcLdr.accumQty <= 0 )
				{
					delPrcLdrList.add(prcLdr.prior);
				}
			}
			else
			{
				break;
			}
		}

		// 删除用完的对手方价格档位并回收到池中
		Iterator<Long> myIter = delPrcLdrList.iterator();
		while(myIter.hasNext())
		{
			PriceLeader rmvLdr = peerPrcLdrTree.remove(myIter.next());
			prcLdrPool.putObj(rmvLdr);
		}
		
		if(this.evtCbs != null)
		{
			evtCbs.noMoreMatch(order);
		}			
		
		if(order.remQty > 0)
		{
			if(insertOrder(order))
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		else
		{
			return true;
		}
	}


	/**
	 *  这里是缺省的计算订单优先级的实现
	 */
	protected final long calcBasePrior(boolean isbuy, long price)
	{
		long basePrior = 0;
		
		if(isbuy)
		{
			basePrior = -price;
		}
		else
		{
			basePrior = price;
		}
		
		return basePrior;		
	}
	
	/**
	 * 以下是Matcher 对于 BizAdaptor 的自带实现. 4个函数
	 */
	@Override
	public final long calcPrior(Order order) 
	{
		return calcBasePrior(order.isbuy, order.price);
	}

	@Override
	public final long calcMaxPrior(boolean isbuy, long price) 
	{
		return calcBasePrior(isbuy, price);
	}

	
	@Override
	public final long ordPrc2Price(long ordPrc) 
	{
		return ordPrc;
	}

	@Override
	public final long price2OrdPrc(long price) 
	{
		return price;
	}
	
	/**
	 *  min() and max() are provided here to utilize 'final' key word
	 */
	private final long min(long a, long b)
	{
		if(a < b)
		{
			return a;
		}
		else
		{
			return b;
		}
	}

	private final long max(long a, long b)
	{
		if(a > b)
		{
			return a;
		}
		else
		{
			return b;
		}
	}	
	
	/**
	 * 虚拟计算集合竞价，但不进行任何撮合匹配
	 */
	public final boolean calcCallAuction(TradedInst stock, CallAuctionResult result)
	{
		TreeMap<Long, PriceLeader> buyTree = null;
		TreeMap<Long, PriceLeader> sellTree = null;
		Map.Entry<Long, PriceLeader> buyEntry = null;
		Map.Entry<Long, PriceLeader> sellEntry = null;
		PriceLeader buyLdr = null;
		PriceLeader sellLdr = null;		
		Long buyKey = null;
		Long sellKey = null;
		
		if(stock == null || result == null)
		{
			return false;
		}
		
		// TREE
		buyTree = stock.buyPrcList;
		if(buyTree == null)
		{
			return false;
		}
		
		sellTree = stock.sellPrcList;
		if(sellTree == null)
		{
			return false;
		}
		
		// LEADER
		buyEntry = buyTree.firstEntry();
		if(buyEntry == null)
		{
			return false;
		}		
		buyKey = buyEntry.getKey();
		buyLdr = buyEntry.getValue();
		
		sellEntry = sellTree.firstEntry();
		if(sellEntry == null)
		{
			return false;
		}
		sellKey = sellEntry.getKey();
		sellLdr = sellEntry.getValue();

		if(sellLdr.price > buyLdr.price)
		{
			// 订单簿完全没有交叉
			return false;
		}

		// 从买卖队列的头开始遍历
		long totalMatchedQty = 0;
		long lastBuyPrice = 0;
		long lastSellPrice = 0;
		long buyQtyRemain = buyLdr.accumQty;
		long sellQtyRemain = sellLdr.accumQty;
		
		while(buyLdr.price >= sellLdr.price)
		{
			long matchOnce = min(buyQtyRemain, sellQtyRemain);
			
			totalMatchedQty += matchOnce;
			buyQtyRemain -= matchOnce;
			sellQtyRemain -= matchOnce;
			
			lastBuyPrice = buyLdr.price;
			lastSellPrice = sellLdr.price;
			
			if(buyQtyRemain == 0)
			{
				buyEntry = stock.buyPrcList.higherEntry(buyKey);
				if(buyEntry == null)
				{
					break;
				}
				else
				{
					buyKey = buyEntry.getKey();
					buyLdr = buyEntry.getValue();
					
					buyQtyRemain = buyLdr.accumQty;
				}
			}
			
			if(sellQtyRemain == 0)
			{
				sellEntry = stock.sellPrcList.higherEntry(sellKey);
				if(sellEntry == null)
				{
					break;
				}
				else
				{
					sellKey = sellEntry.getKey();
					sellLdr = sellEntry.getValue();
					
					sellQtyRemain = sellLdr.accumQty;					
				}
			}
		}
		
		// get all 4 prices!
		long headBuyPrice = 0;
		long headSellPrice = 0;
		
		if(buyEntry == null)
		{
			headBuyPrice = Long.MIN_VALUE;
		}
		else
		{
			headBuyPrice = buyEntry.getValue().price;
		}
		
		if(sellEntry == null)
		{
			headSellPrice = Long.MAX_VALUE;
		}
		else
		{
			headSellPrice = sellEntry.getValue().price;
		}
		
		long P1 = min(lastBuyPrice, headSellPrice);
		long P2 = max(lastSellPrice, headBuyPrice);
		
		if(P1 != P2)
		{
			result.price = (P1 + P2) / 2;
		}
		else
		{
			result.price = P1;
		}
		result.volume = totalMatchedQty;
		result.ordPrc = bizAdpt.price2OrdPrc(result.price);

		return true;
	}
	
	/**
	 * 真正根据result的指示完成集合竞价
	 */
	public final boolean doCallAuction(TradedInst stock, CallAuctionResult result)
	{
		PriceLeader buyLdr = null;
		PriceLeader sellLdr = null;
		Order buyOrd = null;
		Order sellOrd = null;
		
		if(stock == null || result == null)
		{
			return false;
		}

		if(result.volume <= 0)
		{
			// 订单簿完全没有交叉
			return true;
		}
		
		long remainQty = result.volume;
		
		Iterator<Map.Entry<Long, PriceLeader>> itsB =  stock.buyPrcList.entrySet().iterator();
		Iterator<Map.Entry<Long, PriceLeader>> itsS =  stock.sellPrcList.entrySet().iterator();
		
		if(!itsB.hasNext())
		{
			return false;
		}
		else
		{
			buyLdr = itsB.next().getValue();
		}		

		if(!itsS.hasNext())
		{
			return false;
		}		
		else
		{
			sellLdr = itsS.next().getValue();
		}
		
		delPrcLdrList.clear();
		delPrcLdrList2.clear();
		
		boolean nextBuyOrd = true;
		boolean nextSellOrd = true;
		long qty = 0;
		
		while(remainQty > 0)
		{
			// Get an buy order when needed
			while(nextBuyOrd == true)
			{
				buyOrd = buyLdr.orderList.poll();
				if(buyOrd == null)
				{
					if(!itsB.hasNext())
					{
						return false;
					}
					else
					{
						buyLdr = itsB.next().getValue();
						continue;
					}
				}
				else
				{
					if(buyOrd.delflg == true)
					{
						continue;
					}
					else
					{
						break;
					}
				}
			}
			
			// Get a sell order when needed
			while(nextSellOrd == true)
			{
				sellOrd = sellLdr.orderList.poll();
				if(sellOrd == null)
				{
					if(!itsS.hasNext())
					{
						return false;
					}
					else
					{
						sellLdr = itsS.next().getValue();	
						continue;
					}
				}
				else
				{
					if(sellOrd.delflg == true)
					{
						continue;
					}
					else
					{
						break;
					}
				}
			}
			
			// match their quantity
			qty = min(buyOrd.remQty, sellOrd.remQty);
			qty = min(qty, remainQty);
			
			buyOrd.remQty -= qty;
			buyLdr.accumQty -= qty;
			
			if(buyLdr.accumQty <= 0)
			{
				delPrcLdrList.add(buyLdr.prior);
			}
			
			sellOrd.remQty -= qty;
			sellLdr.accumQty -= qty;

			if(sellLdr.accumQty <= 0)
			{
				delPrcLdrList2.add(sellLdr.prior);
			}			
			
			remainQty -= qty;
			
			if(this.evtCbs != null)
			{
				evtCbs.callAuctionMatch(buyOrd, sellOrd, qty, result.price);
				
			}
			
			if(buyOrd.remQty <= 0)
			{
				if(this.evtCbs != null)
				{
					evtCbs.leaveOrderBook(buyOrd);
				}	
				
				nextBuyOrd = true;
			}
			else
			{
				nextBuyOrd = false;
			}
			
			if(sellOrd.remQty <= 0)
			{
				if(this.evtCbs != null)
				{
					evtCbs.leaveOrderBook(sellOrd);
				}	
				
				nextSellOrd = true;
			}
			else
			{
				nextSellOrd = false;
			}
		}
		
		// Delete and recycle PRICE LEADERS
		Iterator<Long> myIter = delPrcLdrList.iterator();
		while(myIter.hasNext())
		{
			PriceLeader rmvLdr = stock.buyPrcList.remove(myIter.next());
			prcLdrPool.putObj(rmvLdr);
		}		
		
		myIter = delPrcLdrList2.iterator();
		while(myIter.hasNext())
		{
			PriceLeader rmvLdr = stock.sellPrcList.remove(myIter.next());
			prcLdrPool.putObj(rmvLdr);
		}		
		
		return true;
	}
	
	
}

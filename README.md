README
===

###需要解决的问题

使用ViewPager做多个页面Fragment视图时，最小的缓存数量只能是1，即使复写了ViewPager将最小缓存数量降低到0，新划出的界面也是黑色的，并不符合一般的设计需求。

如果被缓存的Fragment中有大量的数据查询或网络请求等耗费资源的操作，那么必然造成了不必要的资源损耗，如果用户根本没有打算滑动到这个界面，那么`预先加载的数据完全是浪费`。

###解决方案

在ViewPager滑动的过程中，其Adapter会被委托处理子Fragment事件，而每次滑动结果产生，Adapter都会通过调用Fragment的`setUserVisibleHint`来告知Fragment其当前是否真正展现在用户眼前。所以通过复写Fragment的setUserVisibleHint方法，再匹配以适当的逻辑处理就可以达到我们想要的效果——只有当Fragment真正可视时才加载数据。

	
	@Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && isResumed()) {
            //做数据加载
        }
    }
    
不要忽略了isResumed，因为setUserVisibleHint被调用的时候可能Fragment并没有attach到视图上。一般这种情况会出现在第一个Fragment中，可以通过识别position为0解决，具体可查看代码。
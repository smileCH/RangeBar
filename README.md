项目中的酒店模块有个双向滑动的价格选择器控件，感觉不是很满意，所以就趁着刚发完版本这段空闲时间自己重新自定义了一个，效果和瓜子二手车中的价格选择器有点相似，啰嗦了半天，客官消消气，小的这就上图：![range_price.gif](https://upload-images.jianshu.io/upload_images/11695905-19ef4b260650c2ea.gif?imageMogr2/auto-orient/strip)从效果图上可以大致发现，此控件的一些**特点**
1、可以灵活设置步长值，开发者只需要传入最小值、最大值以及每一步代表的数值即可
2、当滑动距离小于步长距离的一半时松开会自动回弹到上一个位置处，相反则会自动回弹到下一个位置处
3、当两圆不在两极端位置时（即：两圆在起始位置和终点位置之间），当滑动左边圆圈靠近到右边圆时，继续右滑则左边圆位于之前右边圆的位置不再动，而右边圆则会继续向右边滑动；滑动右边圆时同理
4、当滑动左边圆到达最右边圆的终点位置时再向左滑动，右边圆不动，左边圆继续向左滑动，右边圆情况同理
5、允许两圆相重合，重合时则代表的数值相同
这样说可能还是不太好理解，我们把每个圆用不同的颜色来区分开，如下图所示![range_bar.gif](https://upload-images.jianshu.io/upload_images/11695905-5b60363df770c543.gif?imageMogr2/auto-orient/strip)这样再看是不是很直观了
之前对于自定义View也写了很多文章了，如果有兴趣的话可以到我的[CSDN博客](https://blog.csdn.net/xiaxiazaizai01)中去了解下，其实一般的自定义控件都需要这几步，**首先**，对需求仔细研究思考并且在自己的本子上画画草图进行结构分析(很有用)，**然后**就是自定义属性了，假如你打算开源的话，那么自定义属性是必不可少的，这样便于使用者根据自己需要进行灵活设置，**接着**就是测量了，确定控件的宽高，**紧接着**就可以进行绘制了，当然了如果是继承ViewGroup的话则还需要去确定子View的位置，如果涉及到手势滑动等操作，**最后**还需要对手势滑动事件进行一系列的处理等等。当然了，这只是一个大致的步骤，因人而异吧，那么我们就按这个大致的步骤一点点的分析吧
#### 自定义属性
```
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <declare-styleable name="RangeBarView">
        <attr name="rect_line_height" format="dimension"/>
        <attr name="rect_line_default_color" format="color"/>
        <attr name="rect_line_checked_color" format="color"/>
        <attr name="circle_radius" format="dimension"/>
        <attr name="circle_stroke_width" format="dimension"/>
        <attr name="left_circle_solid_color" format="color"/>
        <attr name="left_circle_stroke_color" format="color"/>
        <attr name="right_circle_solid_color" format="color"/>
        <attr name="right_circle_stroke_color" format="color"/>
        <attr name="range_text_size" format="dimension"/>
        <attr name="range_text_color" format="color"/>
        <attr name="view_text_space" format="dimension"/>
        <attr name="rect_price_desc_dialog_width" format="dimension"/>
        <attr name="rect_price_desc_dialog_color" format="color"/>
        <attr name="rect_price_desc_dialog_corner_radius" format="dimension"/>
        <attr name="rect_price_desc_text_size" format="dimension"/>
        <attr name="rect_price_desc_text_color" format="color"/>
        <attr name="rect_price_desc_space_to_progress" format="dimension"/>
    </declare-styleable>
</resources>
```
属性有点多哈，具体的就不再详细说了，对着上面的效果图再加上在下这拙劣的英文相信大家都能见（委）文（屈）识（各）意（位）了。![淡淡的忧伤.png](https://upload-images.jianshu.io/upload_images/11695905-5987ecd9023205ca.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)接着我们看下如何去获取这些自定义的属性
```
   public RangeBarView(Context context) {
        this(context, null);
    }

    public RangeBarView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RangeBarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //初始化属性值，同时将每一个属性的默认值设置在styles.xml文件中
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RangeBarView, 0, R.style.default_range_bar_value);
        int count = typedArray.getIndexCount();
        for (int i = 0; i < count; i++) {
            int attr = typedArray.getIndex(i);
            switch (attr) {
                case R.styleable.RangeBarView_rect_line_default_color:
                    rectLineDefaultColor = typedArray.getColor(attr, ContextCompat.getColor(context, R.color.color_cdcd));
                    break;
                case R.styleable.RangeBarView_rect_line_checked_color:
                    rectLineCheckedColor = typedArray.getColor(attr, ContextCompat.getColor(context, R.color.color_275D9D));
                    break;
                case R.styleable.RangeBarView_rect_line_height:
                    rectLineHeight = typedArray.getDimensionPixelSize(attr, getResources().getDimensionPixelSize(R.dimen.rect_line_height));
                    break;
                case R.styleable.RangeBarView_circle_radius:
                    circleRadius = typedArray.getDimensionPixelSize(attr, getResources().getDimensionPixelSize(R.dimen.circle_radius));
                    break;
                case R.styleable.RangeBarView_circle_stroke_width:
                    circleStrokeWidth = typedArray.getDimensionPixelSize(attr, getResources().getDimensionPixelSize(R.dimen.circle_stroke_width));
                    break;
                case R.styleable.RangeBarView_left_circle_solid_color:
                    leftCircleSolidColor = typedArray.getColor(attr, ContextCompat.getColor(context, R.color.color_fff));
                    break;
                case R.styleable.RangeBarView_left_circle_stroke_color:
                    leftCircleStrokeColor = typedArray.getColor(attr, ContextCompat.getColor(context, R.color.color_cdcd));
                    break;
                case R.styleable.RangeBarView_right_circle_solid_color:
                    rightCircleSolidColor = typedArray.getColor(attr, ContextCompat.getColor(context, R.color.color_fff));
                    break;
                case R.styleable.RangeBarView_right_circle_stroke_color:
                    rightCircleStrokeColor = typedArray.getColor(attr, ContextCompat.getColor(context, R.color.color_cdcd));
                    break;
                case R.styleable.RangeBarView_range_text_size:
                    textSize = typedArray.getDimensionPixelSize(attr, getResources().getDimensionPixelSize(R.dimen.item_text_size));
                    break;
                case R.styleable.RangeBarView_range_text_color:
                    textColor = typedArray.getColor(attr, ContextCompat.getColor(context, R.color.color_333));
                    break;
                case R.styleable.RangeBarView_view_text_space:
                    spaceDistance = typedArray.getDimensionPixelSize(attr, getResources().getDimensionPixelSize(R.dimen.view_and_text_space));
                    break;
                case R.styleable.RangeBarView_rect_price_desc_dialog_width:
                    rectDialogWidth = typedArray.getDimensionPixelSize(attr, getResources().getDimensionPixelSize(R.dimen.rect_dialog_width));
                    break;
                case R.styleable.RangeBarView_rect_price_desc_dialog_color:
                    rectDialogColor = typedArray.getColor(attr, ContextCompat.getColor(context, R.color.color_275D9D));
                    break;
                case R.styleable.RangeBarView_rect_price_desc_dialog_corner_radius:
                    rectDialogCornerRadius = typedArray.getDimensionPixelSize(attr, getResources().getDimensionPixelSize(R.dimen.rect_dialog_corner_radius));
                    break;
                case R.styleable.RangeBarView_rect_price_desc_text_size:
                    rectDialogTextSize = typedArray.getDimensionPixelSize(attr, getResources().getDimensionPixelSize(R.dimen.rect_dialog_text_size));
                    break;
                case R.styleable.RangeBarView_rect_price_desc_text_color:
                    rectDialogTextColor = typedArray.getColor(attr, ContextCompat.getColor(context, R.color.color_fff));
                    break;
                case R.styleable.RangeBarView_rect_price_desc_space_to_progress:
                    rectDialogSpaceToProgress = typedArray.getDimensionPixelSize(attr, getResources().getDimensionPixelSize(R.dimen.rect_dialog_space_to_progress));
                    break;
            }
        }
        typedArray.recycle();
        //初始化画笔
        initPaints();
    }
```
如果开发者忘记在布局文件中去设置这些自定义属性的话，我们需要给每个属性一个默认值，这里在styles.xml文件中进行统一配置各个属性默认值
```
<style name="default_range_bar_value">
        <item name="rect_line_height">5dp</item>
        <item name="rect_line_default_color">#CDCDCD</item>
        <item name="rect_line_checked_color">#275D9D</item>
        <item name="circle_radius">10dp</item>
        <item name="circle_stroke_width">2dp</item>
        <item name="left_circle_solid_color">#D10773</item>
        <item name="left_circle_stroke_color">#275D9D</item>
        <item name="right_circle_solid_color">#4499FF</item>
        <item name="right_circle_stroke_color">#275D9D</item>
        <item name="range_text_size">16sp</item>
        <item name="range_text_color">#333333</item>
        <item name="view_text_space">10dp</item>
        <item name="rect_price_desc_dialog_width">85dp</item>
        <item name="rect_price_desc_dialog_color">#275D9D</item>
        <item name="rect_price_desc_dialog_corner_radius">15dp</item>
        <item name="rect_price_desc_text_size">12sp</item>
        <item name="rect_price_desc_text_color">#FFFFFF</item>
        <item name="rect_price_desc_space_to_progress">5dp</item>
    </style>
```
如果没有在xml布局文件中设置自定义属性的话，代码是不会走到for循环中的，到此，自定义属性啰嗦完了，不过还是要提下注意点，首先获取完自定义属性后要记得将TypedArray对象回收，即typedArray.recycle();其次，我们可以在此构造方法中进行画笔等的初始化工作，所以，相信大家都知道为什么不能在onDraw方法中去实例化画笔等对象了(因为onDraw会被多次调用，这样就会new出来大量的对象)
#### 测量
```
@Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width, height;
        int wSize = getPaddingLeft() + circleRadius*2 + getPaddingRight() + circleStrokeWidth*2;
        int hSize = getPaddingTop() + rectDialogCornerRadius*2 + triangleHeight + rectDialogSpaceToProgress + circleRadius*2 + circleStrokeWidth*2 + spaceDistance + textSize + getPaddingBottom();

        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            width = Math.min(widthSize, wSize);
        }else {
            width = wSize;
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            height = Math.min(heightSize, hSize);
        }else {
            height = hSize;
        }
        Log.e("TAG", "宽onMeasure----> "+width);
        setMeasuredDimension(width, height);
    }
```
测量的时候需要对宽高的不同Mode进行不同的处理，主要有三种方式EXACTLY、AT_MOST和UNSPECIFIED这几种模式相信大家都很熟悉了，这里不再啰嗦了。
#### onSizeChanged方法
```
@Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        //控件实际宽度 = 宽度w(包含内边距的) - paddingLeft - paddingRight;
        Log.e("TAG", "宽----> "+w);

        realWidth = w - getPaddingLeft() - getPaddingRight();
        strokeRadius = circleRadius + circleStrokeWidth;
        rectDialogHeightAndSpace = rectDialogCornerRadius*2 + rectDialogSpaceToProgress;
        //左边圆的圆心坐标
        leftCircleObj = new CirclePoint();
        leftCircleObj.cx = getPaddingLeft() + strokeRadius;
        leftCircleObj.cy = getPaddingTop() + rectDialogHeightAndSpace + strokeRadius;
        //右边圆的圆心坐标
        rightCircleObj = new CirclePoint();
        rightCircleObj.cx = w - getPaddingRight() - strokeRadius;
        rightCircleObj.cy = getPaddingTop() + rectDialogHeightAndSpace + strokeRadius;
        //默认圆角矩形进度条
        rectLineCornerRadius = rectLineHeight / 2;//圆角半径
        defaultCornerLineRect.left = getPaddingLeft() + strokeRadius;
        defaultCornerLineRect.top = getPaddingTop() + rectDialogHeightAndSpace + strokeRadius - rectLineCornerRadius;
        defaultCornerLineRect.right = w - getPaddingRight() - strokeRadius;
        defaultCornerLineRect.bottom = getPaddingTop() + rectDialogHeightAndSpace + strokeRadius + rectLineCornerRadius;
        //选中状态圆角矩形进度条
        selectedCornerLineRect.left = leftCircleObj.cx;
        selectedCornerLineRect.top = getPaddingTop() + rectDialogHeightAndSpace + strokeRadius - rectLineCornerRadius;
        selectedCornerLineRect.right = rightCircleObj.cx;
        selectedCornerLineRect.bottom = getPaddingTop() + rectDialogHeightAndSpace + strokeRadius + rectLineCornerRadius;
        //数值描述圆角矩形
        numberDescRect.left = w / 2 - rectDialogWidth/2;
        numberDescRect.top = getPaddingTop();
        numberDescRect.right = w / 2 + rectDialogWidth/2;
        numberDescRect.bottom = getPaddingTop() + rectDialogCornerRadius*2;
        //每一份对应的距离
        perSlice = (realWidth - strokeRadius*2) / slice;
    }
```
我们可以在onSizeChanged方法中配置控件的初始状态等，在这里我们可以看到我们实例化了两个对象leftCircleObj = new CirclePoint();和rightCircleObj = new CirclePoint();正所谓一切事物皆对象(**面向对象编程**)，按照我之前的写法会分别画左边圆和右边圆，这样无疑产生了很多重复代码，因为两个圆本质上是一样的。通过对象的方式便于管理，使用起来也很方便，当然了，也是从阅读很多优秀大牛写的自定义控件中学到的。此控件不是那么复杂，所以在设计圆对象的时候只声明了圆心坐标俩变量，不仅如此，比如我们也可以将滑动数据等统一放到一个对象中进行管理
```
private class CirclePoint{
        //圆的圆心坐标
        public int cx;
        public int cy;
    }
```
#### 绘制
```
@Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //绘制中间圆角矩形线
        drawDefaultCornerRectLine(canvas);
        //绘制两圆之间的圆角矩形
        drawSelectedRectLine(canvas);
        //画左边圆以及圆的边框
        drawLeftCircle(canvas);
        //画右边圆以及圆的边框
        drawRightCircle(canvas);
        //绘制文字
        drawBottomText(canvas);
        //绘制描述信息圆角矩形弹窗
        drawRectDialog(canvas);
        //绘制描述信息弹窗中的文字
        drawTextOfRectDialog(canvas);
        //绘制小三角形
        drawSmallTriangle(canvas);
    }
```
下面分别展示下这些组件的绘制，其实主要是坐标的确定，只要坐标知道了，那么绘制起来就一气呵成了。其中，绘制小三角形用的是path进行连线绘制
```
private void drawDefaultCornerRectLine(Canvas canvas) {
        canvas.drawRoundRect(defaultCornerLineRect, rectLineCornerRadius, rectLineCornerRadius, defaultLinePaint);
    }

    private void drawSelectedRectLine(Canvas canvas) {
        canvas.drawRoundRect(selectedCornerLineRect, rectLineCornerRadius, rectLineCornerRadius, selectedLinePaint);
    }
    
    private void drawLeftCircle(Canvas canvas) {
        canvas.drawCircle(leftCircleObj.cx, leftCircleObj.cy, circleRadius, leftCirclePaint);
        canvas.drawCircle(leftCircleObj.cx, leftCircleObj.cy, circleRadius, leftCircleStrokePaint);
    }

    private void drawRightCircle(Canvas canvas) {
        canvas.drawCircle(rightCircleObj.cx, rightCircleObj.cy, circleRadius, rightCirclePaint);
        canvas.drawCircle(rightCircleObj.cx, rightCircleObj.cy, circleRadius, rightCircleStrokePaint);
    }

    private void drawBottomText(Canvas canvas) {
        textPaint.setColor(textColor);
        textPaint.setTextSize(textSize);
        for (int i = 0; i <=slice; i++) {
            int value = i*sliceValue > maxValue ? maxValue : i*sliceValue + minValue;
            String text = String.valueOf(value);
            float textWidth = textPaint.measureText(text);
            canvas.drawText(text, i*perSlice - textWidth/2 + (getPaddingLeft() + strokeRadius), getPaddingTop()+rectDialogHeightAndSpace+strokeRadius*2+spaceDistance+textSize/2, textPaint);
        }
    }

    private void drawRectDialog(Canvas canvas) {
        if (isShowRectDialog) {
            canvas.drawRoundRect(numberDescRect, rectDialogCornerRadius, rectDialogCornerRadius, selectedLinePaint);
        }
    }
    
    private void drawTextOfRectDialog(Canvas canvas) {
        if (leftValue == minValue && (rightValue == maxValue || rightValue < maxValue)) {
            textDesc = rightValue+"万以下";
        } else if (leftValue > minValue && rightValue == maxValue) {
            textDesc = leftValue+"万以上";
        } else if (leftValue > minValue && rightValue < maxValue) {
            if (leftValue == rightValue) {
                textDesc = rightValue+"万以下";
            }else
                textDesc = leftValue+"-"+rightValue+"万";
        }

        if (isShowRectDialog) {
            textPaint.setColor(rectDialogTextColor);
            textPaint.setTextSize(rectDialogTextSize);
            float textWidth = textPaint.measureText(textDesc);
            float textLeft = numberDescRect.left + rectDialogWidth/2 - textWidth/2;
            canvas.drawText(textDesc, textLeft, getPaddingTop()+rectDialogCornerRadius+rectDialogTextSize/4, textPaint);
        }
    }

    private void drawSmallTriangle(Canvas canvas) {
        if (isShowRectDialog) {
            trianglePath.reset();
            trianglePath.moveTo(numberDescRect.left + rectDialogWidth/2 - triangleLength/2, getPaddingTop() + rectDialogCornerRadius*2);
            trianglePath.lineTo(numberDescRect.left + rectDialogWidth/2 + triangleLength/2, getPaddingTop() + rectDialogCornerRadius*2);
            trianglePath.lineTo(numberDescRect.left + rectDialogWidth/2, getPaddingTop() + rectDialogCornerRadius*2+triangleHeight);
            trianglePath.close();
            canvas.drawPath(trianglePath, selectedLinePaint);
        }
    }
```
#### 然后是对手势滑动的处理
对于手势滑动的处理还是比较麻烦和繁琐的，首先我们需要确定当前滑动的是左边圆还是右边圆，可以通过如下方式进行判断，为了方便大家理解，我在代码中写了详细的注释，一次无意间的机会看到[CodeCopyer](https://blog.csdn.net/givemeacondom/article/details/80991034)大牛的文章中关于点击属于哪个位置用到了**Region**（Region表示多个图形组成的区域范围，一般判断某一点(按下的坐标)是否在某一个区域范围内），又get到了一项技能，在这里表示感谢，感兴趣的小伙伴可以用此方式实现下
```
private boolean checkIsLeftOrRight(float downX) {
        //如果按下的区域位于左边区域，则按下坐标downX的值就会比较小(即按下坐标点在左边)，那么leftCircleObj.cx - downX的绝对值也会比较小
        //rightCircleObj.cx - downX绝对值肯定是大于leftCircleObj.cx - downX绝对值的，两者相减肯定是小于0的
        if (Math.abs(leftCircleObj.cx - downX) - Math.abs(rightCircleObj.cx - downX) > 0) {//表示按下的区域位于右边
            return false;
        }
        return true;
    }
```
接着需要对起始位置以及终点位置的**边界进行处理，防止越界**，两圆总不能滑出起始位置或者终点位置吧，其实对边界的处理还是很简单的，比如左边圆滑动坐标小于起始点的坐标时，我们就把起始点的坐标重新赋值给这个圆的圆心坐标，右边临界点的判断也是类似
```
       //防止越界处理
        if (touchLeftCircle) {
            if (leftCircleObj.cx > rightCircleObj.cx) {
                leftCircleObj.cx = rightCircleObj.cx;
            }else {
                if (leftCircleObj.cx < getPaddingLeft() + strokeRadius) {
                    leftCircleObj.cx = getPaddingLeft() + strokeRadius;
                }
                if (leftCircleObj.cx > getWidth() - getPaddingRight() - strokeRadius) {
                    leftCircleObj.cx = getWidth() - getPaddingRight() - strokeRadius;
                }
            }
        }else {
            if (leftCircleObj.cx > rightCircleObj.cx) {
                rightCircleObj.cx  = leftCircleObj.cx;
            }else {
                if (rightCircleObj.cx > getWidth() - getPaddingRight() - strokeRadius) {
                    rightCircleObj.cx = getWidth() - getPaddingRight() - strokeRadius;
                }

                if (rightCircleObj.cx < getPaddingLeft() + strokeRadius) {
                    rightCircleObj.cx = getPaddingLeft() + strokeRadius;
                }
            }
        }
```

以及两圆相遇时的处理，总不能确认过眼神就是对的人吧，这块处理需要特别的注意，就像文章开头对控件的分析中说到的几种情况，①两圆在起始位置处相遇②两圆在终点位置处相遇③两圆在中间某一处相遇。对于情况①和②情况比较相似，这里就统一啰嗦下，滑动右边圆在起始位置处和左边圆相遇后，再次向右滑动，那么要保证左边圆不动，右边圆继续向右滑动；当左边圆在终点处与右边圆相遇时也是同样道理。对于情况③当滑动左边圆在中间某一处与右边圆相遇时，继续向右滑动，注意此时左边圆停留在之前右边圆的位置处不动，而右边圆则继续向右滑动（根据图二可以很清晰的观察出滑动的规律），用代码表示就是在move时进行判断处理
```
            case MotionEvent.ACTION_MOVE:
                float moveX = event.getX();
                isShowRectDialog = true;
                if (leftCircleObj.cx == rightCircleObj.cx) {//两圆圈重合的情况
                    if (touchLeftCircle) {
                        //极端情况的优化处理，滑动左边圆到达最右边时，再次滑动时设置为左滑，即：继续让左边圆向左滑动
                        if (leftCircleObj.cx == getWidth() - getPaddingRight() - strokeRadius) {
                            touchLeftCircle = true;
                            leftCircleObj.cx = (int) moveX;
                        }else {
                            //当滑动左边圆在中间某处与右边圆重合时，此时再次继续滑动则左边圆处于右边圆位置处不动，右边圆改为向右滑动
                            touchLeftCircle = false;
                            rightCircleObj.cx = (int) moveX;
                        }
                    }else {
                        if (rightCircleObj.cx == getPaddingLeft() + strokeRadius) {
                            touchLeftCircle = false;
                            rightCircleObj.cx = (int) moveX;
                        }else {
                            touchLeftCircle = true;
                            leftCircleObj.cx = (int) moveX;
                        }
                    }
                }else {
                    if (touchLeftCircle) {
                        //滑动左边圆圈时，如果位置等于或者超过右边圆的位置时，设置右边圆圈的坐标给左边圆圈，就相当于左边圆圈停留在右边圆圈之前的位置上，然后移动右边圆圈
                        leftCircleObj.cx = leftCircleObj.cx - rightCircleObj.cx >= 0 ? rightCircleObj.cx : (int) moveX;
                    }else {
                        //同理
                        rightCircleObj.cx = rightCircleObj.cx - leftCircleObj.cx <= 0 ? leftCircleObj.cx : (int) moveX;
                    }
                }

                break;
```

其次还要保证在两圆滑动的过程中最上方显示价格信息描述的圆角矩形弹窗始终位于两圆的中间位置，还有就是文章开头分析的，当滑动的距离小于步长的一半时，松开滑动，圆需要回弹到上一个位置处，很多细节都是需要处理的，在写的过程中会遇到很多奇葩的问题，需要有足够的耐心慢慢去调试，最后我们需要在手指抬起也就是up时将数据回调给UI进行展示
```
            case MotionEvent.ACTION_UP:
                if (touchLeftCircle) {
                    int partsOfLeft = getSliceByCoordinate((int) event.getX());
                    leftCircleObj.cx = leftCircleObj.cx - rightCircleObj.cx >= 0 ? rightCircleObj.cx : partsOfLeft*perSlice+strokeRadius;
                }else {
                    int partsOfRight = getSliceByCoordinate((int) event.getX());
                    rightCircleObj.cx = rightCircleObj.cx - leftCircleObj.cx <= 0 ? leftCircleObj.cx : partsOfRight*perSlice+strokeRadius;
                }

                int leftData = getSliceByCoordinate(leftCircleObj.cx)*sliceValue + minValue;
                int rightData = getSliceByCoordinate(rightCircleObj.cx)*sliceValue + minValue;
                leftValue = leftData > maxValue ? maxValue : leftData;
                rightValue = rightData > maxValue ? maxValue : rightData;
                //回调
                if (listener != null) {
                    listener.onMoveValue(leftValue, rightValue);
                }
                break;
        }
```
最后，我们再来看下布局文件以及Activity中如何调用展示
```
<com.ch.test.RangeBarView
        android:id="@+id/view_range_bar"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="50dp"
        app:rect_line_default_color="@color/color_cdcd"
        app:rect_line_checked_color="@color/color_275D9D"
        app:left_circle_solid_color="@color/color_fff"
        app:left_circle_stroke_color="@color/color_cdcd"
        app:right_circle_solid_color="@color/color_fff"
        app:right_circle_stroke_color="@color/color_cdcd"
        app:circle_stroke_width="2dp"
        app:circle_radius="15dp"
        app:rect_line_height="3dp"
        android:layout_marginLeft="20dp"
        android:layout_marginRight="20dp"
        />
```
以及Activity
```
        int minValue = 0;
        int maxValue = 100;
        int sliceValue = 20;
        tvLeftValue.setText(minValue+"");
        tvRightValue.setText(maxValue+"");
        rangeBarView.setDatas(minValue, maxValue, sliceValue, new RangeBarView.OnMoveValueListener() {
            @Override
            public void onMoveValue(int leftValue, int rightValue) {
                tvLeftValue.setText("左边值为：" + leftValue + "--> "+leftValue);
                tvRightValue.setText("右边值为：" + rightValue + "--> "+rightValue);
            }
        });
```
我们可以看到开发者使用起来也比较方便，这里不但需要一个最大值还需要一个最小值，这样设计的目的是，有的需求并不是从0到某一个数值，比如，也有可能是从50-1000这样的，同时还需要告诉程序小圆每移动一下代表的数值是多少，然后根据这些数据可以算出此控件在此数据范围内一共可以分多少份，对于除不尽的话我们会增加一份来表示剩下的一点数据。这里还要感谢Nipuream老铁的文章给的灵感，感兴趣的话可以[点击这里查看大牛文章](https://blog.csdn.net/yanghuinipurean/article/details/52336810)，今天就先写到这吧，同事都早已下班回去嗨皮的过双休了，我也要撤了，代码写的有些匆忙，难免会存在些问题，如有问题欢迎大家提出，我们共同交流处理

简书地址：[https://www.jianshu.com/u/22e84d1967f4](https://www.jianshu.com/u/22e84d1967f4)





package com.jtkj.library.commom.lockpattern;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;

import com.jtkj.library.R;

/**
 * nine block box lock
 * @author Sym
 */
public class LockPatternViewValidate extends LockPatternView {

	public LockPatternViewValidate(Context context) {
		this(context, null);
	}

	public LockPatternViewValidate(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public LockPatternViewValidate(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		this.init();
	}

	/**
	 * initialize
     */
	private void init(){
		this.initCellSize();
		this.init9Cells();
		this.initPaints();
		this.initPaths();
		this.initMatrixs();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		drawToCanvas(canvas);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		initCellSize();
		set9CellsSize();
		this.invalidate();
	}
	
	/**
	 * initialize paints
	 */
	private void initPaints(){
		defaultPaint = new Paint();
		defaultPaint.setColor(getResources().getColor(R.color.color_white));
		defaultPaint.setStrokeWidth(2.0f);
		defaultPaint.setStyle(Style.FILL);
		defaultPaint.setAntiAlias(true);

		selectPaint = new Paint();
		selectPaint.setColor(getResources().getColor(R.color.color_white));
		selectPaint.setStrokeWidth(3.0f);
		selectPaint.setStyle(Style.STROKE);
		selectPaint.setAntiAlias(true);

		selectInnerPaint = new Paint();
		selectInnerPaint.setColor(getResources().getColor(R.color.color_white));
		selectInnerPaint.setStrokeWidth(3.0f);
		selectInnerPaint.setStyle(Style.FILL);
		selectInnerPaint.setAntiAlias(true);

		errorPaint = new Paint();
		errorPaint.setColor(getResources().getColor(R.color.red_f3323b));
		errorPaint.setStrokeWidth(3.0f);
		errorPaint.setAntiAlias(true);

		linePaint = new Paint();
		linePaint.setColor(getResources().getColor(R.color.color_white));
		linePaint.setStrokeWidth(6.0f);
		linePaint.setStyle(Style.FILL);
		linePaint.setAntiAlias(true);

		errorLinePaint = new Paint();
		errorLinePaint.setColor(getResources().getColor(R.color.red_f3323b));
		errorLinePaint.setStrokeWidth(6.0f);
		errorLinePaint.setStyle(Style.FILL);
		errorLinePaint.setAntiAlias(true);
	}

	/**
	 * draw the view to canvas
	 * @param canvas
	 */
	protected void drawToCanvas(Canvas canvas){

		for (int i = 0; i < mCells.length; i++) {
			for (int j = 0; j < mCells[i].length; j++) {
				if (mCells[i][j].getStatus() == Cell.STATE_CHECK) {
					canvas.drawCircle(mCells[i][j].getX(), mCells[i][j].getY(),
							this.cellRadius, this.selectPaint);
					canvas.drawCircle(mCells[i][j].getX(), mCells[i][j].getY(),
							this.cellInnerRadius, this.selectInnerPaint);
				} else if (mCells[i][j].getStatus() == Cell.STATE_NORMAL) {
					canvas.drawCircle(mCells[i][j].getX(), mCells[i][j].getY(),
							this.cellInnerRadius, this.defaultPaint);
				} else if (mCells[i][j].getStatus() == Cell.STATE_CHECK_ERROR) {
					errorPaint.setStyle(Style.STROKE);
					canvas.drawCircle(mCells[i][j].getX(), mCells[i][j].getY(),
							this.cellRadius, this.errorPaint);
					errorPaint.setStyle(Style.FILL);
					canvas.drawCircle(mCells[i][j].getX(), mCells[i][j].getY(),
							this.cellInnerRadius, this.errorPaint);
				}
			}
		}

		if (sCells.size() > 0) {
			//temporary cell: at the beginning the cell is the first of sCells
			Cell tempCell = sCells.get(0);

			for (int i = 1; i < sCells.size(); i++) {
				Cell cell = sCells.get(i);
				if (cell.getStatus() == Cell.STATE_CHECK) {
					//drawLine(tempCell, cell, canvas, linePaint);
					drawLineNotIncludeCircle(tempCell, cell, canvas, linePaint);
				} else if (cell.getStatus() == Cell.STATE_CHECK_ERROR) {
					//drawLine(tempCell, cell, canvas, errorLinePaint);
					drawLineNotIncludeCircle(tempCell, cell, canvas, errorLinePaint);
				}
				tempCell = cell;
			}

			if (isActionMove && !isActionUp) {
				//drawLineFollowFinger(tempCell, canvas, linePaint);
				drawLineFollowFinger(tempCell, canvas, linePaint);
			}
		}
	}
}

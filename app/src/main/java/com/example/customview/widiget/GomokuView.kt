package com.example.customview.widiget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.example.customview.utils.fdp
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * @author wandervogel
 * @date 2026-02-02  星期一
 * @description
 */
class GomokuView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    companion object {
        const val EMPTY = 0
        const val BLACK = 1
        const val WHITE = 2
    }

    // 网格数,默认15 x 15
    var boardSize: Int = 15
        set(value) {
            require(value >= 5) { "boardSize must be >= 5" }
            field = value
            reset()
        }


    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1.fdp
    }
    private val blackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val whitePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val whiteStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1.fdp
    }
    private val lastMovePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2.fdp
    }
    // 网格大小
    private var cellSize = 0f
    private var startX = 0f
    private var startY = 0f
    // 棋子半径
    private var pieceRadius = 0f
    // 记录整个棋盘落子状态
    private var board: Array<IntArray> = Array(boardSize) { IntArray(boardSize) }
    private val moves = ArrayList<Move>() // 用于悔棋
    var currentPlayer: Int = BLACK
        private set
    var gameOver: Boolean = false
        private set

    private var lastMove: Move? = null

    // 回调：胜利/落子
    var onGameOver: ((winner: Int) -> Unit)? = null
    var onMove: ((row: Int, col: Int, player: Int) -> Unit)? = null

    init {
        blackPaint.color = 0xFF111111.toInt()
        whitePaint.color = 0xFFF2F2F2.toInt()
        whiteStrokePaint.color = 0xFF999999.toInt()
        gridPaint.color = 0xFF333333.toInt()
        lastMovePaint.color = 0xFFFF6A00.toInt()
    }

    data class Move(val row: Int, val col: Int, val player: Int)


    fun reset() {
        board = Array(boardSize) { IntArray(boardSize) }
        moves.clear()
        currentPlayer = BLACK
        gameOver = false
        lastMove = null
        invalidate()
    }

    /**
     * 悔棋
     */
    fun undo(): Boolean {
        if (moves.isEmpty() || gameOver && moves.isEmpty()) return false
        // 若已经gameOver，也允许悔棋继续玩
        val m = moves.removeAt(moves.lastIndex)
        board[m.row][m.col] = EMPTY
        currentPlayer = m.player // 悔回到该步玩家
        gameOver = false
        lastMove = moves.lastOrNull()
        invalidate()
        return true
    }

    fun getCell(row: Int, col: Int): Int = board[row][col]

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = MeasureSpec.getSize(widthMeasureSpec)
        val h = MeasureSpec.getSize(heightMeasureSpec)
        val size = min(w, h)
        setMeasuredDimension(size, size)// 保持为正方形，取宽高中小的边
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        val size = min(w, h).toFloat()

        // 留边,让棋子不贴边
        val padding = 16.fdp
        // 出去padding的可用空间
        val usable = size - padding * 2

        // 交叉点是 boardSize 个，间距是 (boardSize - 1)
        cellSize = usable / (boardSize - 1)// 计算每个格子大小
        startX = padding
        startY = padding
        pieceRadius = cellSize * 0.42f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        drawGrid(canvas)
        drawPieces(canvas)
        drawLastMoveHint(canvas)
    }

    /**
     * 绘制棋盘
     */
    private fun drawGrid(canvas: Canvas) {
        // 画 boardSize 条横线 + boardSize 条竖线
        val endX = startX + cellSize * (boardSize - 1)
        val endY = startY + cellSize * (boardSize - 1)

        for (i in 0 until boardSize) {
            val x = startX + i * cellSize
            val y = startY + i * cellSize
            canvas.drawLine(startX, y, endX, y, gridPaint) // 横
            canvas.drawLine(x, startY, x, endY, gridPaint) // 竖
        }
    }

    /**
     * 绘制棋子
     */
    private fun drawPieces(canvas: Canvas) {
        for (r in 0 until boardSize) {
            for (c in 0 until boardSize) {
                when (board[r][c]) {
                    BLACK -> {
                        val p = cellToPoint(r, c)
                        canvas.drawCircle(p.x, p.y, pieceRadius, blackPaint)
                    }

                    WHITE -> {
                        val p = cellToPoint(r, c)
                        canvas.drawCircle(p.x, p.y, pieceRadius, whitePaint)
                        canvas.drawCircle(p.x, p.y, pieceRadius, whiteStrokePaint)
                    }
                }
            }
        }
    }

    /**
     * 显示最后一步落子
     */
    private fun drawLastMoveHint(canvas: Canvas) {
        val m = lastMove ?: return
        val p = cellToPoint(m.row, m.col)
        val r = pieceRadius * 0.55f
        canvas.drawCircle(p.x, p.y, r, lastMovePaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action != MotionEvent.ACTION_UP) return true
        if (gameOver) return true

        val (row, col) = pointToCell(event.x, event.y) ?: return true
        if (board[row][col] != EMPTY) return true

        placePiece(row, col, currentPlayer)
        return true
    }

    private fun placePiece(row: Int, col: Int, player: Int) {
        board[row][col] = player
        val m = Move(row, col, player)
        moves.add(m)
        lastMove = m
        onMove?.invoke(row, col, player)

        if (checkWin(row, col, player)) {
            gameOver = true
            invalidate()
            onGameOver?.invoke(player)
            return
        }

        // 切换玩家
        currentPlayer = if (currentPlayer == BLACK) WHITE else BLACK
        invalidate()
    }

    // 胜负判定（从落子点向四方向统计）
    private fun checkWin(row: Int, col: Int, player: Int): Boolean {
        val directions = arrayOf(
            intArrayOf(1, 0),   // 横
            intArrayOf(0, 1),   // 竖
            intArrayOf(1, 1),   // 主对角
            intArrayOf(1, -1)   // 副对角
        )
        for (d in directions) {
            val count = 1 +
                    countOneDirection(row, col, d[0], d[1], player) +
                    countOneDirection(row, col, -d[0], -d[1], player)
            if (count >= 5) return true
        }
        return false
    }

    /**
     * 统计单一方向同色棋子连接数
     */
    private fun countOneDirection(
        row: Int,
        col: Int,
        dr: Int,
        dc: Int,
        player: Int
    ): Int {
        var r = row + dr
        var c = col + dc
        var cnt = 0
        while (r in 0 until boardSize && c in 0 until boardSize && board[r][c] == player) {
            cnt++
            r += dr
            c += dc
        }
        return cnt
    }

    // 坐标转换
    private fun cellToPoint(row: Int, col: Int): PointF {
        return PointF(
            startX + col * cellSize,
            startY + row * cellSize
        )
    }

    private fun pointToCell(x: Float, y: Float): Pair<Int, Int>? {
        // 粗略判定是否在棋盘范围附近（容忍半个格）
        val minX = startX - cellSize * 0.5f
        val minY = startY - cellSize * 0.5f
        val maxX = startX + cellSize * (boardSize - 1) + cellSize * 0.5f
        val maxY = startY + cellSize * (boardSize - 1) + cellSize * 0.5f
        if (x !in minX..maxX || y !in minY..maxY) return null

        val col = ((x - startX) / cellSize).roundToInt()
        val row = ((y - startY) / cellSize).roundToInt()
        if (row !in 0 until boardSize || col !in 0 until boardSize) return null

        // 限制必须点击到交叉点附近（避免误触）
        val p = cellToPoint(row, col)
        val distOk = abs(x - p.x) <= cellSize * 0.45f && abs(y - p.y) <= cellSize * 0.45f
        if (!distOk) return null

        return row to col
    }

}


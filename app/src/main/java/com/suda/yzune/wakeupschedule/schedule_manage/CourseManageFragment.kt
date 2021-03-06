package com.suda.yzune.wakeupschedule.schedule_manage

import android.appwidget.AppWidgetManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.suda.yzune.wakeupschedule.R
import com.suda.yzune.wakeupschedule.base_view.BaseFragment
import com.suda.yzune.wakeupschedule.bean.CourseBaseBean
import com.suda.yzune.wakeupschedule.course_add.AddCourseActivity
import es.dmoral.toasty.Toasty
import splitties.activities.start

class CourseManageFragment : BaseFragment() {

    private val viewModel by activityViewModels<ScheduleManageViewModel>()
    private var tablePosition = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_course_manage, container, false)
        val rvCourseList = view.findViewById<RecyclerView>(R.id.rv_course_list)
        tablePosition = arguments!!.getInt("position")
        viewModel.getCourseBaseBeanListByTable(viewModel.tableSelectList[tablePosition].id).observe(this, Observer {
            if (it == null) return@Observer
            viewModel.courseList.clear()
            viewModel.courseList.addAll(it)
            if (rvCourseList.adapter == null) {
                initRecyclerView(rvCourseList, viewModel.courseList)
            } else {
                rvCourseList!!.adapter!!.notifyDataSetChanged()
            }
        })
        return view
    }

    private fun initRecyclerView(rvCourseList: RecyclerView, data: MutableList<CourseBaseBean>) {
        rvCourseList.layoutManager = LinearLayoutManager(context)
        val adapter = CourseListAdapter(R.layout.item_course_list, data)
        adapter.addChildClickViewIds(R.id.ib_edit, R.id.ib_delete)
        adapter.addChildLongClickViewIds(R.id.ib_delete)
        adapter.setOnItemChildClickListener { _, view, position ->
            when (view.id) {
                R.id.ib_edit -> {
                    activity!!.start<AddCourseActivity> {
                        putExtra("id", data[position].id)
                        putExtra("tableId", data[position].tableId)
                        putExtra("maxWeek", viewModel.tableSelectList[tablePosition].maxWeek)
                        putExtra("nodes", viewModel.tableSelectList[tablePosition].nodes)
                    }
                }
                R.id.ib_delete -> {
                    Toasty.info(activity!!.applicationContext, "长按删除课程哦~").show()
                }
            }
        }
        adapter.setOnItemChildLongClickListener { _, view, position ->
            when (view.id) {
                R.id.ib_delete -> {
                    launch {
                        viewModel.deleteCourse(data[position])
                        val list = viewModel.getScheduleWidgetIds()
                        val appWidgetManager = AppWidgetManager.getInstance(activity!!.applicationContext)
                        list.forEach {
                            when (it.detailType) {
                                0 -> appWidgetManager.notifyAppWidgetViewDataChanged(it.id, R.id.lv_schedule)
                                1 -> appWidgetManager.notifyAppWidgetViewDataChanged(it.id, R.id.lv_course)
                            }
                        }
                    }
                    return@setOnItemChildLongClickListener true
                }
                else -> {
                    return@setOnItemChildLongClickListener false
                }
            }

        }
        adapter.addFooterView(initFooterView())
        rvCourseList.adapter = adapter
    }

    private fun initFooterView(): View {
        val view = LayoutInflater.from(context).inflate(R.layout.item_add_course_btn, null)
        val tvBtn = view.findViewById<MaterialButton>(R.id.tv_add)
        tvBtn.text = "添加"
        tvBtn.setOnClickListener {
            activity!!.start<AddCourseActivity> {
                putExtra("id", -1)
                putExtra("tableId", viewModel.tableSelectList[tablePosition].id)
                putExtra("maxWeek", viewModel.tableSelectList[tablePosition].maxWeek)
                putExtra("nodes", viewModel.tableSelectList[tablePosition].nodes)
            }
        }
        return view
    }
}

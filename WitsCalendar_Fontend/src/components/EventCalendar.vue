<template>
  <div style="height: 100vh; width: 100%">
    <FullCalendar ref="fullCalendar" :options="calendarOptions" />

    <div
      v-if="isPopoverVisible"
      class="custom-popover"
      :style="{ left: `${popoverX}px`, top: `${popoverY}px` }"
    >
      <div class="popover-content">
        <el-button v-if="isDeleteOptionVisible" type="danger" @click="deleteSelectedEvent">
          刪除事件
        </el-button>
        <el-button v-if="isDeleteOptionVisible" type="warning" @click="showEditEventDialog">
          修改事件
        </el-button>
        <el-button v-if="!isDeleteOptionVisible" type="primary" @click="showAddEventDialog">
          新增事件
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, onBeforeUnmount, markRaw } from 'vue'
import FullCalendar from '@fullcalendar/vue3'
import dayGridPlugin from '@fullcalendar/daygrid'
import timeGridPlugin from '@fullcalendar/timegrid'
import listGridPlugin from '@fullcalendar/list'
import interactionPlugin from '@fullcalendar/interaction'
import axios from 'axios'
import { format } from 'date-fns'
import { ElMessageBox, ElMessage } from 'element-plus'
import { Delete } from '@element-plus/icons-vue'
import _ from 'lodash'

const emit = defineEmits(['date-click', 'edit-event', 'event-click'])
const fullCalendar = ref(null)
const isPopoverVisible = ref(false)
const isDeleteOptionVisible = ref(false)
const popoverX = ref(0)
const popoverY = ref(0)
const selectedDate = ref('')
const selectedEvent = ref(null)
const events = ref([])

let viewStart
let viewEnd

const debouncedEventChange = _.debounce(async function (info) {
  const googleEventId = info.event.extendedProps.extendsProps?.id

  const isAllDay = info.event.extendedProps.allDay
  let start = info.event.start
  let end = info.event.end

  if (!end) {
    if (isAllDay) {
      end = new Date(start)
    } else {
      end = new Date(start.getTime() + 60 * 60 * 1000)
    }
  }

  if (googleEventId) {
    try {
      const requestData = {
        newSummary: info.event.title,
        newDescription: info.event.extendedProps.description || '',
        newStart: start,
        newEnd: end,
        allDay: isAllDay
      }

      await axios.put(`/events/${googleEventId}`, requestData, {
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${localStorage.getItem('authToken')}`
        }
      })

      ElMessage({
        type: 'success',
        grouping: true,
        message: '修改事件成功'
      })
      await fetchEvents()
    } catch (error) {
      ElMessage({
        type: 'error',
        grouping: true,
        message: '修改事件失敗，請稍候再重試'
      })
    }
  } else {
    ElMessage({
      type: 'error',
      grouping: true,
      message: '修改事件失敗，請稍候再重試'
    })
  }
}, 500)

const calendarOptions = ref({
  plugins: [dayGridPlugin, interactionPlugin, timeGridPlugin, listGridPlugin],
  initialView: 'dayGridMonth',
  locale: 'zh-tw',
  headerToolbar: {
    left: 'prev,next today',
    center: 'title',
    right: 'dayGridMonth,timeGridWeek,timeGridDay,listMonth'
  },

  datesSet: async function (info) {
    const dateRange = {
      startDate: info.start.toISOString(),
      endDate: info.end.toISOString()
    }

    viewStart = info.start.toISOString()
    viewEnd = info.end.toISOString()

    try {
      const response = await axios.get('/events', {
        params: {
          startDate: dateRange.startDate,
          endDate: dateRange.endDate
        },
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${localStorage.getItem('authToken')}`
        }
      })

      let fetchedevents = []

      for (const event of response.data) {
        fetchedevents.push(formattedEvent(event))
      }

      events.value = fetchedevents
    } catch (error) {
      console.error('Failed to fetch events:', error)
    }
  },

  dateClick: (info) => {
    try {
      isPopoverVisible.value = false
      selectedDate.value = info.dateStr
      emit('date-click', info.dateStr)
    } catch (error) {
      ElMessage({
        type: 'warning',
        grouping: true,
        message: '請點擊 all-day 區域以新增事件'
      })
    }
  },
  eventClick: (info) => {
    emit('event-click', info.event)
  },
  eventDidMount: (info) => {
    info.el.addEventListener('contextmenu', (e) => {
      e.preventDefault()
      handleContextMenuForEvent(e, info.event)
    })
  },
  eventRemove: async function (info) {
    const googleEventId = info.event.extendedProps.extendsProps?.id

    if (googleEventId) {
      try {
        await axios.delete('/events', {
          params: { eventId: googleEventId },
          headers: {
            Authorization: `Bearer ${localStorage.getItem('authToken')}`
          }
        })

        ElMessage({
          type: 'success',
          message: '刪除成功！'
        })
      } catch (error) {
        ElMessage({
          type: 'error',
          message: '刪除事件失敗，請稍候再重試'
        })
        info.revert()
      }
    } else {
      ElMessage({
        type: 'error',
        message: '刪除事件失敗，請稍候再重試'
      })
    }
  },
  eventChange: debouncedEventChange,

  eventAdd: async function (info) {
    const isAllDay = info.event.allDay
    let start = info.event.start
    let end = info.event.end

    if (!end) {
      if (isAllDay) {
        end = new Date(start)
      } else {
        end = new Date(start.getTime() + 60 * 60 * 1000)
      }
    }

    const newEvent = {
      newSummary: info.event.title,
      newDescription: info.event.extendedProps.description || '',
      newStart: start,
      newEnd: end,
      allDay: isAllDay
    }

    try {
      await axios.post('/events', newEvent, {
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${localStorage.getItem('authToken')}`
        }
      })

      ElMessage({
        type: 'success',
        message: '事件已成功添加。'
      })
      await fetchEvents()
    } catch (error) {
      ElMessage({
        message: '事件新增失敗，請重試。',
        type: 'error',
        duration: 3000
      })
      info.revert()
    }
  },
  events: events.value
})

onMounted(() => {
  document.addEventListener('contextmenu', (e) => {
    if (!e.target.closest('.fc-event')) {
      handleContextMenu(e)
    }
  })
  document.addEventListener('click', handleDocumentClick)
})

onBeforeUnmount(() => {
  document.removeEventListener('contextmenu', handleContextMenu)
  document.removeEventListener('click', handleDocumentClick)
})

watch(events, (newEvents) => {
  if (fullCalendar.value) {
    const api = fullCalendar.value.getApi()
    api.removeAllEvents()
    api.addEventSource(newEvents)
  }
})

const fetchEvents = async () => {
  const dateRange = {
    startDate: viewStart,
    endDate: viewEnd
  }

  try {
    const response = await axios.get('/events', {
      params: {
        startDate: dateRange.startDate,
        endDate: dateRange.endDate
      },
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${localStorage.getItem('authToken')}`
      }
    })

    let fetchedevents = []

    for (const event of response.data) {
      fetchedevents.push(formattedEvent(event))
    }

    events.value = fetchedevents
  } catch (error) {
    console.error('Failed to fetch events:', error)
  }
}

const handleDocumentClick = (event) => {
  if (!event.target.closest('.custom-popover') && !event.target.closest('.fc-daygrid-day')) {
    isPopoverVisible.value = false
  }
}

const handleContextMenu = (event) => {
  event.preventDefault()
  popoverX.value = event.clientX
  popoverY.value = event.clientY
  isPopoverVisible.value = true
  isDeleteOptionVisible.value = false
  selectedDate.value = null
  const hitEl = document.elementFromPoint(event.clientX, event.clientY)

  if (hitEl && (hitEl.classList.contains('fc-daygrid-day') || hitEl.closest('.fc-daygrid-day'))) {
    const dateEl = hitEl.classList.contains('fc-daygrid-day')
      ? hitEl
      : hitEl.closest('.fc-daygrid-day')
    const dateStr = dateEl.getAttribute('data-date')
    if (dateStr) {
      selectedDate.value = dateStr
    } else {
      isPopoverVisible.value = false
    }
  } else {
    ElMessage({
      type: 'warning',
      grouping: true,
      message: '請點擊 all-day 區域以新增事件'
    })
    isPopoverVisible.value = false
  }
}

const handleContextMenuForEvent = (e, event) => {
  e.preventDefault()

  popoverX.value = e.clientX
  popoverY.value = e.clientY
  isPopoverVisible.value = true

  if (event) {
    isDeleteOptionVisible.value = true
    selectedEvent.value = event
  } else {
    isDeleteOptionVisible.value = false
    selectedEvent.value = null
  }
}

const deleteSelectedEvent = () => {
  if (selectedEvent.value) {
    isDeleteOptionVisible.value = false
    ElMessageBox.confirm('是否確定要刪除該事件？', '警告', {
      type: 'warning',
      icon: markRaw(Delete)
    })
      .then(() => {
        selectedEvent.value.remove()

        isPopoverVisible.value = false
        selectedEvent.value = null
      })
      .catch(() => {
        ElMessage({
          type: 'info',
          message: '已取消刪除'
        })
      })
  }
}

const showAddEventDialog = () => {
  isPopoverVisible.value = false
  emit('date-click', selectedDate.value)
}

const showEditEventDialog = () => {
  isPopoverVisible.value = false
  emit('edit-event', selectedEvent.value)
}

const addEvent = (event) => {
  if (fullCalendar.value) {
    const api = fullCalendar.value.getApi()
    const newEvent = {
      title: event.title,
      start: event.startDate,
      end: event.endDate,
      allday: event.allDay,
      description: event.description
    }

    api.addEvent(newEvent)
  }
}

const editEvent = (updatedEvent) => {
  if (!updatedEvent.id) {
    console.error('No event ID provided for update.')
    return
  }

  if (fullCalendar.value) {
    const api = fullCalendar.value.getApi()
    const existingEvent = api.getEventById(updatedEvent.id)

    if (existingEvent) {
      let changesMade = false

      if (existingEvent.extendedProps.allDay !== updatedEvent.allDay) {
        existingEvent.setExtendedProp('allDay', updatedEvent.allDay)
        changesMade = true
      }

      if (existingEvent.title !== updatedEvent.title) {
        existingEvent.setProp('title', updatedEvent.title)
        changesMade = true
      }

      if (
        existingEvent.start !== updatedEvent.startDate ||
        existingEvent.end !== updatedEvent.endDate
      ) {
        existingEvent.setDates(updatedEvent.startDate, updatedEvent.endDate)
        changesMade = true
      }

      if (existingEvent.extendedProps.description !== updatedEvent.description) {
        existingEvent.setExtendedProp('description', updatedEvent.description)
        changesMade = true
      }

      if (changesMade) {
        ElMessage({
          type: 'success',
          message: '修改事件成功'
        })
      } else {
        ElMessage({
          type: 'error',
          grouping: true,
          message: '請稍候再重試修改'
        })
      }
    } else {
      ElMessage({
        type: 'error',
        grouping: true,
        message: '請稍候再重試修改'
      })
    }
  } else {
    ElMessage({
      type: 'error',
      grouping: true,
      message: '請稍候再重試修改'
    })
  }
}

// format event data to match FullCalendar API
const formattedEvent = (event) => {
  const { summary, start, end, id, description } = event
  const hasDateTime = !!start.dateTime

  return hasDateTime
    ? {
        id: id,
        title: summary,
        start: formattedDateTime(start.dateTime.value),
        end: formattedDateTime(end.dateTime.value),
        description: description,
        allDay: event.allDay || false,
        extendsProps: { id }
      }
    : {
        id: id,
        title: summary,
        start: formattedDate(start.date.value),
        end: formattedDate(end.date.value),
        description: description,
        allDay: true,
        extendsProps: { id }
      }
}

// format date to match FullCalendar API
const formattedDate = (timestamp) => {
  const date = new Date(timestamp)
  return format(date, 'yyyy-MM-dd')
}

const formattedDateTime = (timestamp) => {
  return format(new Date(timestamp), 'yyyy-MM-dd HH:mm:ss')
}

defineExpose({ addEvent, editEvent })
</script>

<style scoped>
.custom-popover {
  position: fixed;
  width: 80px;
  background-color: white;
  border: 1px solid #ccc;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  z-index: 1000;
  padding: 10px;
  border-radius: 4px;
  flex-direction: column;
}

.popover-content {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.el-button {
  width: 100%;
  /* @important == override element plus default css */
  margin-left: 0px !important;
}
</style>

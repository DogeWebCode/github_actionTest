<template>
  <div id="app">
    <Calendar
      ref="calendar"
      @date-click="handleDateClick"
      @edit-event="handleEditEvent"
      @event-click="handleEventClick"
    />

    <!-- add & edit event dialog -->
    <EventDialog
      v-model:visible="isDialogVisible"
      :event="selectedEventData"
      :mode="dialogMode"
      @add-event="addEvent"
      @edit-event="editEvent"
    />

    <!-- event detail dialog -->
    <EventDetailDialog v-model:visible="isDetailDialogVisible" :event="selectedEventData" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import Calendar from '../components/EventCalendar.vue'
import EventDialog from '../components/EventDialog.vue'
import EventDetailDialog from '../components/EventDetailDialog.vue'

// enum
const Time = {
  ONEHOUR: 60 * 60 * 1000
}

const isDialogVisible = ref(false)
const isDetailDialogVisible = ref(false)
const selectedEventData = ref({
  title: '',
  startDate: '',
  endDate: '',
  description: '',
  allDay: false
})
const calendar = ref(null)
const dialogMode = ref('add')

const handleDateClick = (dateStr) => {
  selectedEventData.value = {
    title: '',
    startDate: '',
    endDate: '',
    description: '',
    allDay: false
  }

  // date formate is 'YYYY-MM-DD'
  const fullDateStr = `${dateStr}T00:00:00`

  const startDate = new Date(fullDateStr)

  const endDate = new Date(startDate.getTime() + Time.ONEHOUR)

  selectedEventData.value.startDate = startDate.toISOString()
  selectedEventData.value.endDate = endDate.toISOString()

  dialogMode.value = 'add'
  isDialogVisible.value = true
}

const handleEditEvent = (event) => {
  selectedEventData.value = {
    id: event.id,
    title: event.title,
    startDate: event.start,
    endDate: event.end,
    description: event.extendedProps.description,
    allDay: event.allDay
  }
  dialogMode.value = 'edit'
  isDialogVisible.value = true
}

const handleEventClick = (event) => {
  selectedEventData.value = {
    title: event.title,
    startDate: event.start,
    endDate: event.end,
    description: event.extendedProps.description,
    allDay: event.allDay
  }
  isDetailDialogVisible.value = true
}

const addEvent = (event) => {
  if (calendar.value) {
    calendar.value.addEvent(event)
  } else {
    console.error('Calendar reference not found')
  }
}

const editEvent = (event) => {
  if (calendar.value) {
    calendar.value.editEvent(event)
  } else {
    console.warn('Event not found')
  }
}

onMounted(() => {})
</script>

<style scoped>
#logo {
  width: 200px;
}
</style>

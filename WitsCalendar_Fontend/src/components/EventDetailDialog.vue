<template>
  <el-dialog title="事件詳細信息" v-model="localVisible" width="30%" @close="handleDialogClose">
    <el-form :model="eventData">
      <el-form-item label="事件標題">
        <el-input v-model="eventData.title" disabled />
      </el-form-item>
      <el-form-item label="開始日期">
        <el-date-picker
          v-model="eventData.startDate"
          :type="eventData.allDay ? 'date' : 'datetime'"
          disabled
        />
      </el-form-item>
      <el-form-item label="結束日期">
        <el-date-picker
          v-model="adjustedEndDate"
          :type="eventData.allDay ? 'date' : 'datetime'"
          disabled
        />
      </el-form-item>
      <el-form-item label="事件說明">
        <el-input type="textarea" v-model="eventData.description" disabled />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="handleClose">關閉</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch, computed } from 'vue'

const props = defineProps({
  visible: Boolean,
  event: Object
})

const emit = defineEmits(['update:visible'])

const localVisible = ref(props.visible)

watch(
  () => props.visible,
  (newVal) => {
    localVisible.value = newVal
  }
)

const eventData = ref({ ...props.event })

watch(
  () => props.event,
  (newEvent) => {
    eventData.value = { ...newEvent }
  },
  { deep: true }
)

// If the event is an all-day event, the adjusted end date will be one day before the original end date.
const adjustedEndDate = computed(() => {
  if (eventData.value.allDay) {
    const adjustedDate = new Date(eventData.value.endDate)
    adjustedDate.setDate(adjustedDate.getDate() - 1)
    return adjustedDate
  }
  return eventData.value.endDate
})

const handleClose = () => {
  emit('update:visible', false)
}

const handleDialogClose = () => {
  emit('update:visible', false)
  eventData.value = {}
}
</script>

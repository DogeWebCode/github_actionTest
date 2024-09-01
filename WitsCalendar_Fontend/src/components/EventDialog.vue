<template>
  <el-dialog
    :title="mode === 'edit' ? '修改事件' : '新增事件'"
    v-model="dialogVisible"
    width="30%"
    @close="resetForm"
  >
    <el-form :model="localEvent">
      <el-form-item label="事件標題">
        <el-input v-model="localEvent.title" />
      </el-form-item>
      <el-form-item label="全天">
        <el-checkbox v-model="localEvent.allDay">全天</el-checkbox>
      </el-form-item>
      <el-form-item label="開始日期">
        <el-date-picker
          v-model="localEvent.startDate"
          :type="localEvent.allDay ? 'date' : 'datetime'"
        />
      </el-form-item>
      <el-form-item label="結束日期">
        <el-date-picker v-model="adjustedEndDate" :type="localEvent.allDay ? 'date' : 'datetime'" />
      </el-form-item>
      <el-form-item>
        <el-input type="textarea" v-model="localEvent.description" placeholder="事件描述" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="handleSaveEvent">
        {{ mode === 'edit' ? '修改' : '新增' }}
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ElMessage } from 'element-plus'
import { ref, watch, computed } from 'vue'

const props = defineProps({
  visible: Boolean,
  event: Object,
  mode: {
    type: String,
    // default to 'add' mode
    default: 'add'
  }
})

const emit = defineEmits(['update:visible', 'add-event', 'edit-event'])

const dialogVisible = ref(props.visible)
const localEvent = ref({ ...props.event })

watch(
  () => props.visible,
  (newVal) => {
    dialogVisible.value = newVal
  }
)

watch(dialogVisible, (newVal) => {
  emit('update:visible', newVal)
})

watch(
  () => props.event,
  (newVal) => {
    localEvent.value = { ...newVal }
  },
  { deep: true }
)

watch(
  () => localEvent.value.allDay,
  (newVal) => {
    if (newVal) {
      if (localEvent.value.startDate) {
        localEvent.value.startDate = new Date(localEvent.value.startDate)
        localEvent.value.startDate.setHours(0, 0, 0, 0)
      }

      if (localEvent.value.endDate) {
        const endDate = new Date(localEvent.value.endDate)
        endDate.setHours(0, 0, 0, 0)
        if (endDate <= localEvent.value.startDate) {
          endDate.setDate(localEvent.value.startDate.getDate() + 1)
        }
        localEvent.value.endDate = endDate
      } else {
        localEvent.value.endDate = new Date(localEvent.value.startDate)
        localEvent.value.endDate.setDate(localEvent.value.startDate.getDate() + 1)
        localEvent.value.endDate.setHours(0, 0, 0, 0)
      }
    } else {
      if (localEvent.value.startDate) {
        localEvent.value.startDate = new Date(localEvent.value.startDate)
      }

      if (localEvent.value.endDate) {
        localEvent.value.endDate = new Date(localEvent.value.endDate)
        localEvent.value.endDate.setDate(localEvent.value.endDate.getDate())
        if (localEvent.value.endDate <= localEvent.value.startDate) {
          localEvent.value.endDate.setTime(localEvent.value.startDate.getTime() + 60 * 60 * 1000)
        }
      }
    }
  },
  { immediate: true }
)

const adjustedEndDate = computed({
  get() {
    if (!localEvent.value.endDate) {
      return localEvent.value.startDate
    }

    if (localEvent.value.allDay) {
      const endDate = new Date(localEvent.value.endDate)
      if (endDate.getHours() === 0 && endDate.getMinutes() === 0 && endDate.getSeconds() === 0) {
        endDate.setDate(endDate.getDate() - 1)
      }
      return endDate
    } else {
      return localEvent.value.endDate
    }
  },
  set(value) {
    if (localEvent.value.allDay) {
      const adjustedValue = new Date(value)
      adjustedValue.setDate(adjustedValue.getDate() + 1)
      adjustedValue.setHours(0, 0, 0, 0)
      localEvent.value.endDate = adjustedValue
    } else {
      localEvent.value.endDate = value
    }
  }
})

const handleSaveEvent = () => {
  if (localEvent.value.title && localEvent.value.startDate && localEvent.value.endDate) {
    const eventData = {
      id: localEvent.value.id,
      title: localEvent.value.title,
      startDate: formatDate(localEvent.value.startDate, localEvent.value.allDay),
      endDate: formatDate(localEvent.value.endDate, localEvent.value.allDay),
      description: localEvent.value.description,
      allDay: localEvent.value.allDay
    }

    if (props.mode === 'edit') {
      emit('edit-event', eventData)
    } else {
      emit('add-event', eventData)
    }

    dialogVisible.value = false
    resetForm()
  } else {
    ElMessage.error('請填寫完整資訊')
  }
}

const TimeConstants = {
  MILLISECONDS_PER_MINUTE: 60 * 1000
}

const formatDate = (date, isAllDay) => {
  const dateObj = typeof date === 'string' ? new Date(date) : date

  if (isAllDay) {
    return new Date(
      // Use local time offset UTC to prevent date offset
      dateObj.getTime() - dateObj.getTimezoneOffset() * TimeConstants.MILLISECONDS_PER_MINUTE
    )
      .toISOString()
      .split('T')[0]
  } else {
    return dateObj.toISOString()
  }
}

const resetForm = () => {
  localEvent.value = JSON.parse(
    JSON.stringify({ title: '', startDate: '', endDate: '', description: '', allDay: false })
  )
}
</script>

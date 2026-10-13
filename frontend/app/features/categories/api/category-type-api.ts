import type { CategoryTypeDTO } from '../model/CategoryTypeDTO'

function isCategoryTypeDTO(value: unknown): value is CategoryTypeDTO {
  return (
    typeof value === 'object'
    && value !== null
    && 'value' in value
    && typeof value.value === 'string'
  )
}

export async function fetchUsedCategoryTypes(
  signal?: AbortSignal,
): Promise<CategoryTypeDTO[]> {
  const response = await fetch('/api/1.0/categories/types/used', { signal })

  if (!response.ok) {
    throw new Error(`Used category types request failed with HTTP ${response.status}`)
  }

  const data: unknown = await response.json()

  if (!Array.isArray(data) || !data.every(isCategoryTypeDTO)) {
    throw new Error('Used category types response has an invalid shape')
  }

  return data
}

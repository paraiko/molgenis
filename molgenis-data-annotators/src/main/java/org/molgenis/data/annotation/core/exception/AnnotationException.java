package org.molgenis.data.annotation.core.exception;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.AttributeMetaData;

import java.util.List;
import java.util.Objects;

public class AnnotationException extends RuntimeException
{
	private final Integer entityNumber;
	private final Entity failedEntity;
	private final String annotatorName;
	private final List<AttributeMetaData> requiredAttributes;

	public AnnotationException(Entity failedEntity, int lineNumber, List<AttributeMetaData> requiredAttributes,
			String annotatorName, Throwable cause)
	{
		super(cause);
		this.failedEntity = failedEntity;
		this.entityNumber = Objects.requireNonNull(lineNumber);
		this.requiredAttributes = requiredAttributes;
		this.annotatorName = annotatorName;
	}

	public AnnotationException(AnnotationException ae)
	{
		this(ae.getFailedEntity(), ae.getEntityNumber(), ae.getRequiredAttributes(), ae.getAnnotatorName(),
				ae.getCause());
	}

	public Integer getEntityNumber()
	{
		return entityNumber;
	}

	public Entity getFailedEntity()
	{
		return failedEntity;
	}

	public List<AttributeMetaData> getRequiredAttributes()
	{
		return requiredAttributes;
	}

	public String getAnnotatorName()
	{
		return annotatorName;
	}
}
